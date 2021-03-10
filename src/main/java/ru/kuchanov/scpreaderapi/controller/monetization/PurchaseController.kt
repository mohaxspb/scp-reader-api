package ru.kuchanov.scpreaderapi.controller.monetization

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.purchase.SubscriptionValidationAttempts
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscription
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscriptionEventHandleAttemptRecord
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.model.dto.monetization.UserSubscriptionsDto
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjectionV2
import ru.kuchanov.scpreaderapi.model.monetization.InappType
import ru.kuchanov.scpreaderapi.model.monetization.Store
import ru.kuchanov.scpreaderapi.model.monetization.huawei.subscription.subevent.HuaweiSubscriptionEventDto
import ru.kuchanov.scpreaderapi.model.monetization.huawei.subscription.subevent.HuaweiSubscriptionEventResponse
import ru.kuchanov.scpreaderapi.model.monetization.huawei.subscription.subevent.StatusUpdateNotification
import ru.kuchanov.scpreaderapi.service.monetization.purchase.SubscriptionValidateAttemptsService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiApiService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiApiService.Companion.ACCOUNT_FLAG_GERMANY_APP_TOUCH
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiMonetizationService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiSubsEventHandleAttemptService
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
        private val errorUtils: ErrorUtils,
        private val objectMapper: ObjectMapper,
        private val log: Logger
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
            log.error("parsedRequest: $parsedRequest")
            //todo go through subscription flow
        } catch (e: Exception) {
            error = e
            log.error("Error while handle huawei subscription event", error)
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
        val verificationResult = when (store) {
            Store.HUAWEI -> huaweiApiService.verifyPurchase(
                    productId,
                    subscriptionId,
                    purchaseType,
                    purchaseToken,
                    accountFlag
            )
            Store.GOOGLE -> TODO()
            Store.AMAZON -> TODO()
            Store.APPLE -> TODO()
        }
        val huaweiSubscriptionResponse = (verificationResult as ValidationResponse.HuaweiSubscriptionResponse)
        //2. Write product info to DB.
        huaweiMonetizationService.saveSubscription(huaweiSubscriptionResponse.androidSubscription!!, user)

        //3. Update user in DB.
        when (purchaseType) {
            InappType.INAPP, InappType.CONSUMABLE -> TODO()
            InappType.SUBS -> {
                updateUserSubscriptionExpiration(user.id)

                return userService.getByIdAsDtoV2(user.id) ?: throw UserNotFoundException()
            }
        }
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
            log.error("userNonExpiredAndValidSubscriptions max expiryTimeMillis: ${maxExpireTimeSub.expiryTimeMillis}")
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
            @RequestParam(defaultValue = "-1") accountFlag: Int
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
        log.error("validateRecentlyExpiredSubsForPeriod: $nowTimeWithoutZone")
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
            log.error("THERE ARE NO RECENTLY EXPIRED SUBSCRIPTIONS TO VALIDATE: $period!")
        } else {
            log.error(
                    recentlyExpiredSubscriptions.joinToString(
                            prefix = "Start validating subs:",
                            separator = "\n",
                            transform = { "${it.id}: ${it.startTimeMillis}/${it.expiryTimeMillis}" }
                    )
            )
        }

        //2. Iterate them, verify and update DB records
        val updatedSubscriptions: List<HuaweiSubscription> = recentlyExpiredSubscriptions.mapNotNull { currentSubscription ->
            //Do not try to validate after 3 days of unsuccessful attempts if period is not Period.WEEK
            val previousAttempts = subscriptionValidateAttemptsService
                    .getByStoreAndSubscriptionId(Store.HUAWEI.name, currentSubscription.id!!)
            if (period != Period.WEEK && previousAttempts != null && previousAttempts.attempts >= MAX_VALIDATION_ATTEMPTS) {
                log.error("MAX VALIDATE ATTEMPTS LIMIT EXCEEDED: $currentSubscription!")
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
                log.error("Error while validate subscription: $currentSubscription, error: $e", e)
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
            val updatedSubscription = huaweiMonetizationService.saveSubscription(huaweiSubscriptionResponse.androidSubscription!!, owner)

            //3. Update user
            updateUserSubscriptionExpiration(owner.id!!)

            updatedSubscription

            // TODO 4. Send push messages to users with subscription update info.
        }

        return updatedSubscriptions
    }
}
