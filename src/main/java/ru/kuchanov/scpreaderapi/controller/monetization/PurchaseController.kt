package ru.kuchanov.scpreaderapi.controller.monetization

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.Application
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.purchase.SubscriptionValidationAttempts
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscription
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscriptionEventHandleAttemptRecord
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscriptionNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.model.dto.monetization.UserSubscriptionsDto
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjectionV2
import ru.kuchanov.scpreaderapi.model.monetization.InappType
import ru.kuchanov.scpreaderapi.model.monetization.Store
import ru.kuchanov.scpreaderapi.model.monetization.huawei.InAppPurchaseData
import ru.kuchanov.scpreaderapi.model.monetization.huawei.subscription.subevent.HuaweiSubscriptionEventDto
import ru.kuchanov.scpreaderapi.model.monetization.huawei.subscription.subevent.HuaweiSubscriptionEventResponse
import ru.kuchanov.scpreaderapi.model.monetization.huawei.subscription.subevent.StatusUpdateNotification
import ru.kuchanov.scpreaderapi.model.monetization.huawei.subscription.subevent.StatusUpdateNotification.NotificationType.*
import ru.kuchanov.scpreaderapi.service.monetization.purchase.SubscriptionValidateAttemptsService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiApiService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiApiService.Companion.ACCOUNT_FLAG_HUAWEI_ID
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiMonetizationService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiSubsEventHandleAttemptService
import ru.kuchanov.scpreaderapi.service.push.AllProvidersMessagingService
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserService
import ru.kuchanov.scpreaderapi.utils.ErrorUtils
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.MONETIZATION + "/" + ScpReaderConstants.Path.PURCHASE)
class PurchaseController @Autowired constructor(
        private val huaweiApiService: HuaweiApiService,
        private val huaweiMonetizationService: HuaweiMonetizationService,
        private val userService: ScpReaderUserService,
        private val subscriptionValidateAttemptsService: SubscriptionValidateAttemptsService,
        private val huaweiSubsEventHandleAttemptService: HuaweiSubsEventHandleAttemptService,
        private val allProvidersMessagingService: AllProvidersMessagingService,
        private val errorUtils: ErrorUtils,
        private val objectMapper: ObjectMapper,
        @Qualifier(Application.HUAWEI_LOGGER) private val huaweiLog: Logger
) {

    companion object {
        /**
         * each 5 minutes, every hour, 3 days
         */
        const val MAX_VALIDATION_ATTEMPTS = 12 * 24 * 3
    }

    private enum class Period {
        MINUTES_5, HOUR, DAY, WEEK
    }

    @PostMapping("/subscriptionEvents/huawei")
    fun huaweiSubscriptionEventsWebHook(
            @RequestBody huaweiSubscriptionEventDto: HuaweiSubscriptionEventDto
    ): HuaweiSubscriptionEventResponse {
        var error: Exception? = null

        try {
            val parsedRequest = objectMapper.readValue(
                    huaweiSubscriptionEventDto.statusUpdateNotification,
                    StatusUpdateNotification::class.java
            )
            huaweiLog.info("parsedRequest: ${objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsedRequest)}")

            when (parsedRequest.notificationType) {
                INITIAL_BUY, CANCEL, RENEWAL_STOPPED, ON_HOLD,
                PAUSED, PAUSE_PLAN_CHANGED, PRICE_CHANGE_CONFIRMED, DEFERRED -> {
                    //nothing to do.
                }
                RENEWAL, RENEWAL_RESTORED, RENEWAL_RECURRING -> {
                    val inAppPurchaseData: InAppPurchaseData = objectMapper.readValue(
                            parsedRequest.latestReceiptInfo!!,
                            InAppPurchaseData::class.java
                    )
                    val huaweiSubId = inAppPurchaseData.subscriptionId!!

                    val huaweiSubscriptionInDb: HuaweiSubscription = huaweiMonetizationService
                            .getHuaweiSubscriptionBySubscriptionId(huaweiSubId)
                            //there can be subscription switch, so check oriSubscriptionId too
                            ?: huaweiMonetizationService
                                    .getHuaweiSubscriptionBySubscriptionId(inAppPurchaseData.oriSubscriptionId!!)
                            ?: throw HuaweiSubscriptionNotFoundException(
                                    "HuaweiSubscription not found for subscriptionId: $huaweiSubId"
                            )
                    val user: User = huaweiMonetizationService
                            .getUserByHuaweiSubscriptionId(huaweiSubscriptionInDb.id!!) ?: throw UserNotFoundException()

                    val updatedUser = applyHuaweiSubscription(
                            huaweiSubId,
                            inAppPurchaseData.purchaseToken,
                            inAppPurchaseData.accountFlag ?: ACCOUNT_FLAG_HUAWEI_ID,
                            user,
                            pushTitle = "Subscription renewal!",
                            pushMessage = "Your subscription was successfully renewed!"
                    )
                    val updatedSubscription = huaweiMonetizationService
                            .getHuaweiSubscriptionBySubscriptionId(huaweiSubId)
                    huaweiLog.info("""
                        Successfully update renewed Huawei subscription (RENEWAL, RENEWAL_RESTORED, RENEWAL_RECURRING)!
                        User: ${updatedUser.id}/${updatedUser.offlineLimitDisabledEndDate}. 
                        Subscription: ${huaweiSubscriptionInDb.id}/${updatedSubscription?.expiryTimeMillis}.
                    """.trimIndent())
                }
                INTERACTIVE_RENEWAL -> {
                    //seems to be, here we can found oriSubscriptionId,
                    //which must be equal to inAppPurchaseDataForPreviousSub.subscriptionId
                    val inAppPurchaseDataForCurrentSub: InAppPurchaseData = objectMapper.readValue(
                            parsedRequest.latestReceiptInfo!!,
                            InAppPurchaseData::class.java
                    )
                    //use it find user by his previous sub
                    @Suppress("DuplicatedCode")
                    val inAppPurchaseDataForPreviousSub: InAppPurchaseData = objectMapper.readValue(
                            parsedRequest.latestExpiredReceiptInfo!!,
                            InAppPurchaseData::class.java
                    )

                    //find user by inAppPurchaseDataForCurrentSub or inAppPurchaseDataForPreviousSub
                    //validate and apply to user. (expired subscription renew or this maybe change to new subscription)

                    val existingHuaweiSubId = inAppPurchaseDataForPreviousSub.subscriptionId!!

                    val huaweiSubscriptionInDb: HuaweiSubscription = huaweiMonetizationService
                            .getHuaweiSubscriptionBySubscriptionId(existingHuaweiSubId)
                            ?: throw HuaweiSubscriptionNotFoundException(
                                    "HuaweiSubscription not found for subscriptionId: $existingHuaweiSubId"
                            )

                    val user: User = huaweiMonetizationService
                            .getUserByHuaweiSubscriptionId(huaweiSubscriptionInDb.id!!) ?: throw UserNotFoundException()

                    val updatedUser = applyHuaweiSubscription(
                            inAppPurchaseDataForCurrentSub.subscriptionId!!,
                            inAppPurchaseDataForCurrentSub.purchaseToken,
                            inAppPurchaseDataForCurrentSub.accountFlag ?: ACCOUNT_FLAG_HUAWEI_ID,
                            user,
                            pushTitle = "Subscription resuming!",
                            pushMessage = "Your subscription was successfully resumed!"
                    )
                    val updatedSubscription = huaweiMonetizationService
                            .getHuaweiSubscriptionBySubscriptionId(inAppPurchaseDataForCurrentSub.subscriptionId)
                    huaweiLog.info("""
                        Successfully update renewed Huawei subscription (INTERACTIVE_RENEWAL)!
                        User: ${updatedUser.id}/${updatedUser.offlineLimitDisabledEndDate}. 
                        Subscription: ${huaweiSubscriptionInDb.id}/${updatedSubscription?.expiryTimeMillis}.
                    """.trimIndent())
                }
                NEW_RENEWAL_PREF -> {
                    //A user selects another subscription in the group and it takes effect
                    //after the current subscription expires.
                    //The current validity period is not affected.
                    //That is, the subscription takes effect in the next validity period after downgrade or crossgrade.
                    //
                    //The notification carries the last valid receipt and new subscription information,
                    //including the product ID and subscription ID.

                    //write to db, connect to user, validate

                    val inAppPurchaseData: InAppPurchaseData = objectMapper.readValue(
                            parsedRequest.latestReceiptInfo!!,
                            InAppPurchaseData::class.java
                    )
                    val huaweiSubId = inAppPurchaseData.subscriptionId!!
                    val huaweiOriSubId = inAppPurchaseData.oriSubscriptionId!!

                    val huaweiSubscriptionInDb: HuaweiSubscription = huaweiMonetizationService
                            .getHuaweiSubscriptionBySubscriptionId(huaweiOriSubId)
                            ?: throw HuaweiSubscriptionNotFoundException(
                                    "HuaweiSubscription not found for huaweiOriSubId: $huaweiOriSubId"
                            )

                    @Suppress("DuplicatedCode")
                    val user: User = huaweiMonetizationService
                            .getUserByHuaweiSubscriptionId(huaweiSubscriptionInDb.id!!) ?: throw UserNotFoundException()

                    @Suppress("DuplicatedCode")
                    val updatedUser = applyHuaweiSubscription(
                            subscriptionId = huaweiSubId,
                            purchaseToken = inAppPurchaseData.purchaseToken,
                            accountFlag = inAppPurchaseData.accountFlag ?: ACCOUNT_FLAG_HUAWEI_ID,
                            user = user,
                            pushTitle = "Subscription plan changed!",
                            pushMessage = "Your subscription was successfully changed to new one!"
                    )
                    val updatedSubscription = huaweiMonetizationService
                            .getHuaweiSubscriptionBySubscriptionId(huaweiSubId)
                    huaweiLog.info("""
                        Successfully save changed Huawei subscription (NEW_RENEWAL_PREF)!
                        User: ${updatedUser.id}/${updatedUser.offlineLimitDisabledEndDate}. 
                        Subscription: ${huaweiSubscriptionInDb.id}/${updatedSubscription?.expiryTimeMillis}.
                    """.trimIndent())
                }
            }
        } catch (e: Exception) {
            error = e
            huaweiLog.error("Error while handle huawei subscription event", error)
        } finally {
            huaweiSubsEventHandleAttemptService.save(
                    HuaweiSubscriptionEventHandleAttemptRecord(
                            statusUpdateNotification = huaweiSubscriptionEventDto.statusUpdateNotification,
                            notificationSignature = huaweiSubscriptionEventDto.notifycationSignature
                    ).apply {
                        error?.let { e ->
                            errorClass = e::class.java.simpleName
                            errorMessage = e.message
                            stacktrace = errorUtils.stackTraceAsString(e)
                            error.cause?.let {
                                causeErrorClass = it::class.java.simpleName
                                causeErrorMessage = it.message
                                causeStacktrace = errorUtils.stackTraceAsString(it)
                            }
                        }
                    }
            )
        }

        return HuaweiSubscriptionEventResponse()
    }

    @PostMapping("/apply/{store}/{purchaseType}")
    fun applyAndroidProduct(
            @PathVariable store: Store,
            @PathVariable purchaseType: InappType,
            @RequestParam productId: String,
            @RequestParam subscriptionId: String,
            @RequestParam purchaseToken: String,
            @RequestParam(defaultValue = ACCOUNT_FLAG_HUAWEI_ID.toString()) accountFlag: Int,
            @AuthenticationPrincipal user: User?
    ): UserProjectionV2 {
        check(user != null) { "User is null!" }
        check(user.id != null) { "User ID is null!" }
        // 1. Verify product
        when (store) {
            Store.HUAWEI -> {
                when (purchaseType) {
                    InappType.SUBS -> {
                        return applyHuaweiSubscription(
                                subscriptionId = subscriptionId,
                                purchaseToken = purchaseToken,
                                accountFlag = accountFlag,
                                user = user,
                                pushTitle = "Subscription is active!",
                                pushMessage = "Your subscription was successfully activated!"
                        )
                    }
                    InappType.INAPP, InappType.CONSUMABLE -> TODO()
                }

            }
            Store.GOOGLE -> TODO()
            Store.AMAZON -> TODO()
            Store.APPLE -> TODO()
        }
    }

    private fun applyHuaweiSubscription(
            subscriptionId: String,
            purchaseToken: String,
            accountFlag: Int,
            user: User,
            pushTitle: String,
            pushMessage: String
    ): UserProjectionV2 {
        // 1. Verify product
        val verificationResult = huaweiApiService.verifyPurchase(
                subscriptionId,
                InappType.SUBS,
                purchaseToken,
                accountFlag
        )

        val huaweiSubscriptionResponse = (verificationResult as ValidationResponse.HuaweiSubscriptionResponse)

        //2. Write product info to DB.
        huaweiMonetizationService.saveSubscription(huaweiSubscriptionResponse.androidSubscription!!, user)

        //3. Update user in DB.
        updateUserSubscriptionExpiration(user.id!!)

        //4. Send push
        allProvidersMessagingService.sendToUser(
                userId = user.id,
                title = pushTitle,
                message = pushMessage,
                type = ScpReaderConstants.Push.MessageType.SUBSCRIPTION_EVENT,
                author = userService.getById(ScpReaderConstants.InternalAuthData.ADMIN_ID)
                        ?: throw UserNotFoundException()
        )

        return userService.getByIdAsDtoV2(user.id) ?: throw UserNotFoundException()
    }

    private fun updateUserSubscriptionExpiration(userId: Long): User {
        val userInDb = userService.getById(userId) ?: throw UserNotFoundException()

        val curTimeMillis = Instant.now().toEpochMilli()
        val userNonExpiredAndValidSubscriptions = huaweiMonetizationService
                .getHuaweiSubscriptionsForUser(userId)
                .filter { it.subIsValid }
                .filter { it.expiryTimeMillis!!.toInstant(ZoneOffset.UTC).toEpochMilli() > curTimeMillis }
                .sortedBy { it.expiryTimeMillis }
        return if (userNonExpiredAndValidSubscriptions.isEmpty()) {
            userInDb
        } else {
            val maxExpireTimeSub = userNonExpiredAndValidSubscriptions.first()
            huaweiLog.info("userNonExpiredAndValidSubscriptions max expiryTimeMillis: ${maxExpireTimeSub.expiryTimeMillis}")
            userService.update(
                    userInDb.apply { offlineLimitDisabledEndDate = maxExpireTimeSub.expiryTimeMillis }
            )
        }
    }

    @GetMapping("/subscription/all")
    fun getUserSubscriptions(
            @AuthenticationPrincipal user: User,
            @RequestParam showAll: Boolean,
    ): UserSubscriptionsDto {
        check(user.id != null) { "User ID is null!" }

        val huaweiSubscriptions = if (showAll) {
            huaweiMonetizationService
                    .getHuaweiSubscriptionsForUser(user.id)
        } else {
            val curTimeMillis = Instant.now().toEpochMilli()
            huaweiMonetizationService
                    .getHuaweiSubscriptionsForUser(user.id)
                    .filter { it.subIsValid }
                    .filter { it.expiryTimeMillis!!.toInstant(ZoneOffset.UTC).toEpochMilli() > curTimeMillis }
        }
                .sortedBy { it.expiryTimeMillis }

        return UserSubscriptionsDto(huaweiSubscriptions = huaweiSubscriptions)
    }

    @GetMapping("/cancel/{store}/{purchaseType}")
    fun cancelProduct(
            @PathVariable store: Store,
            @PathVariable purchaseType: InappType,
            @RequestParam productId: String,
            @RequestParam purchaseToken: String,
            @RequestParam(defaultValue = ACCOUNT_FLAG_HUAWEI_ID.toString()) accountFlag: Int
    ): String {
        val test = when (store) {
            Store.HUAWEI -> huaweiApiService.cancelSubscription(productId, purchaseToken, accountFlag)
            Store.GOOGLE -> TODO()
            Store.AMAZON -> TODO()
            Store.APPLE -> TODO()
        }
        return test.toString()
    }

    @Scheduled(
            /**
             * second, minute, hour, day, month, day of week
             *
             * Each 5 minutes in interval of 1-59 minutes
             */
            cron = "0 1-59/5 * * * *"
    )
    @GetMapping("/subscription/recentlyExpired/minutes5")
    fun periodicallyVerifyPurchaseMinutes5(): List<HuaweiSubscription> =
            validateRecentlyExpiredSubsForPeriod(Period.MINUTES_5)

    @Scheduled(
            /**
             * second, minute, hour, day, month, day of week
             *
             * Each hour except of midnight
             */
            cron = "0 0 1-23 * * *"
    )
    @GetMapping("/subscription/recentlyExpired/hourly")
    fun periodicallyVerifyPurchaseHourly(): List<HuaweiSubscription> =
            validateRecentlyExpiredSubsForPeriod(Period.HOUR)

    @Scheduled(
            /**
             * second, minute, hour, day, month, day of week
             *
             * Each day
             */
            cron = "0 0 0 * * *"
    )
    @GetMapping("/subscription/recentlyExpired/daily")
    fun periodicallyVerifyPurchaseDaily(): List<HuaweiSubscription> =
            validateRecentlyExpiredSubsForPeriod(Period.DAY)

    @Scheduled(
            /**
             * second, minute, hour, day, month, day of week
             *
             * Each day
             */
            cron = "0 0 0 * * *"
    )
    @GetMapping("/subscription/recentlyExpired/dailyForWeek")
    fun periodicallyVerifyPurchaseDailyForWeek(): List<HuaweiSubscription> =
            validateRecentlyExpiredSubsForPeriod(Period.WEEK)

    private fun validateRecentlyExpiredSubsForPeriod(period: Period): List<HuaweiSubscription> {
        val nowTimeWithoutZone = LocalDateTime.now(ZoneOffset.UTC)
        huaweiLog.info("validateRecentlyExpiredSubsForPeriod: $nowTimeWithoutZone")
        val startDate = when (period) {
            Period.MINUTES_5 -> nowTimeWithoutZone.minusMinutes(5)
            Period.HOUR -> nowTimeWithoutZone.minusHours(1)
            Period.DAY -> nowTimeWithoutZone.minusDays(1)
            Period.WEEK -> nowTimeWithoutZone.minusDays(8)
        }
        val endDate = when (period) {
            Period.WEEK -> nowTimeWithoutZone.minusDays(1)
            else -> nowTimeWithoutZone
        }
        val recentlyExpiredSubscriptions = huaweiMonetizationService.getHuaweiSubscriptionsBetweenDates(
                startDate, endDate
        ).sortedByDescending { it.expiryTimeMillis }
        if (recentlyExpiredSubscriptions.isEmpty()) {
            huaweiLog.info("THERE ARE NO RECENTLY EXPIRED SUBSCRIPTIONS TO VALIDATE: $period!")
        } else {
            huaweiLog.info(
                    recentlyExpiredSubscriptions.joinToString(
                            prefix = "Start validating subs:",
                            separator = "\n",
                            transform = { "${it.id}: ${it.startTimeMillis}/${it.expiryTimeMillis}" }
                    )
            )
        }

        //2. Iterate them, verify and update DB records
        @Suppress("UnnecessaryVariable")
        val updatedSubscriptions: List<HuaweiSubscription> = recentlyExpiredSubscriptions.mapNotNull { currentSubscription ->
            //Do not try to validate after 3 days of unsuccessful attempts if period is not Period.WEEK
            val previousAttempts = subscriptionValidateAttemptsService
                    .getByStoreAndSubscriptionId(Store.HUAWEI.name, currentSubscription.id!!)
            if (period != Period.WEEK && previousAttempts != null && previousAttempts.attempts >= MAX_VALIDATION_ATTEMPTS) {
                huaweiLog.error("MAX VALIDATE ATTEMPTS LIMIT EXCEEDED: $currentSubscription!")
                return@mapNotNull null
            }
            val verificationResult: ValidationResponse? = try {
                huaweiApiService.verifyPurchase(
                        currentSubscription.subscriptionId,
                        InappType.SUBS,
                        currentSubscription.purchaseToken,
                        currentSubscription.accountFlag ?: 0
                )
            } catch (e: Exception) {
                huaweiLog.error("Error while validate subscription: $currentSubscription, error: $e", e)
                null
            }
            //increment attempt
            val attemptsRow = subscriptionValidateAttemptsService.getByStoreAndSubscriptionId(
                    Store.HUAWEI.name,
                    currentSubscription.id
            ) ?: SubscriptionValidationAttempts(
                    subscriptionId = currentSubscription.id,
                    attempts = 0,
                    store = Store.HUAWEI.name,
                    lastAttemptTime = Timestamp.valueOf(nowTimeWithoutZone)

            )
            subscriptionValidateAttemptsService.save(
                    attemptsRow.apply {
                        attempts = if (verificationResult != null) 0 else attempts++
                        lastAttemptTime = Timestamp.valueOf(nowTimeWithoutZone)
                    }
            )

            if (verificationResult == null) {
                return@mapNotNull null
            }

            val huaweiSubscriptionResponse = (verificationResult as ValidationResponse.HuaweiSubscriptionResponse)

            //2. Write product info to DB.
            val owner = huaweiMonetizationService.getUserByHuaweiSubscriptionId(currentSubscription.id)
                    ?: throw UserNotFoundException()
            val updatedSubscription = huaweiMonetizationService.saveSubscription(
                    huaweiSubscriptionResponse.androidSubscription!!,
                    owner
            )

            //3. Update user
            updateUserSubscriptionExpiration(owner.id!!)

            // 4. Send push messages to users with subscription update info.
            val title = if (updatedSubscription.subIsValid) {
                "Subscription was successfully updated!"
            } else {
                "Subscription expired!"
            }
            val message = if (updatedSubscription.subIsValid) {
                "Subscription was updated in your profile."
            } else {
                "Subscription has expired and no longer active."
            }

            allProvidersMessagingService.sendToUser(
                    userId = owner.id,
                    title = title,
                    message = message,
                    type = ScpReaderConstants.Push.MessageType.SUBSCRIPTION_EVENT,
                    author = userService.getById(ScpReaderConstants.InternalAuthData.ADMIN_ID)
                            ?: throw UserNotFoundException()
            )

            updatedSubscription

        }

        return updatedSubscriptions
    }
}
