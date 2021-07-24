package ru.kuchanov.scpreaderapi.controller.monetization

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.util.Base64Utils
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.Application
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.ScpReaderConstants.InternalAuthData.ADMIN_ID
import ru.kuchanov.scpreaderapi.bean.purchase.SubscriptionValidationAttempts
import ru.kuchanov.scpreaderapi.bean.purchase.google.GoogleSubscriptionEventHandleAttemptRecord
import ru.kuchanov.scpreaderapi.bean.purchase.google.GoogleSubscriptionNotFoundException
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscription
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscriptionEventHandleAttemptRecord
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscriptionNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.model.dto.monetization.UserSubscriptionsDto
import ru.kuchanov.scpreaderapi.model.dto.purchase.GoogleAcknowledgeResult
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationStatus
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjectionV2
import ru.kuchanov.scpreaderapi.model.monetization.InappType
import ru.kuchanov.scpreaderapi.model.monetization.Store
import ru.kuchanov.scpreaderapi.model.monetization.google.subscription.subevent.DeveloperNotification
import ru.kuchanov.scpreaderapi.model.monetization.google.subscription.subevent.DeveloperNotification.SubscriptionNotification.NotificationType.*
import ru.kuchanov.scpreaderapi.model.monetization.google.subscription.subevent.GoogleSubscriptionEventDto
import ru.kuchanov.scpreaderapi.model.monetization.huawei.InAppPurchaseData
import ru.kuchanov.scpreaderapi.model.monetization.huawei.subscription.subevent.HuaweiSubscriptionEventDto
import ru.kuchanov.scpreaderapi.model.monetization.huawei.subscription.subevent.HuaweiSubscriptionEventResponse
import ru.kuchanov.scpreaderapi.model.monetization.huawei.subscription.subevent.StatusUpdateNotification
import ru.kuchanov.scpreaderapi.model.monetization.huawei.subscription.subevent.StatusUpdateNotification.NotificationType.*
import ru.kuchanov.scpreaderapi.service.monetization.purchase.SubscriptionValidateAttemptsService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google.GooglePurchaseError
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google.GooglePurchaseService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google.GoogleSubsEventHandleAttemptService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google.GoogleSubscriptionService
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
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.MONETIZATION + "/" + ScpReaderConstants.Path.PURCHASE)
class PurchaseController @Autowired constructor(
    //huawei
    private val huaweiApiService: HuaweiApiService,
    private val huaweiMonetizationService: HuaweiMonetizationService,
    private val huaweiSubsEventHandleAttemptService: HuaweiSubsEventHandleAttemptService,
    @Qualifier(Application.HUAWEI_LOGGER) private val huaweiLog: Logger,
    //fucking google
    private val googlePurchaseService: GooglePurchaseService,
    private val googleSubscriptionService: GoogleSubscriptionService,
    private val googleSubsEventHandleAttemptService: GoogleSubsEventHandleAttemptService,
    @Value("\${my.monetization.subscriptions.google.packageName}") private val googlePackageName: String,
    @Qualifier(Application.GOOGLE_LOGGER) private val googleLog: Logger,
    //misc
    private val userService: ScpReaderUserService,
    private val subscriptionValidateAttemptsService: SubscriptionValidateAttemptsService,
    private val allProvidersMessagingService: AllProvidersMessagingService,
    private val errorUtils: ErrorUtils,
    private val objectMapper: ObjectMapper
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

    @PostMapping("/subscriptionEvents/g_purchases")
    fun googleSubscriptionEventsWebHook(
        request: HttpServletRequest
    ) {
        val body = IOUtils.toString(request.inputStream, Charsets.UTF_8)
        googleLog.info("body: $body")
        try {
            request.headerNames.toList().forEach { googleLog.info("headers: $it, ${request.getHeader(it)}") }
        } catch (e: Throwable) {
            googleLog.error("headers error", e)
        }

        try {
            request.parameterMap.entries.forEach { googleLog.info("parameterMap: ${it.key}, ${it.value}") }
        } catch (e: Throwable) {
            googleLog.error("parameterMap error", e)
        }

        if (body.isNullOrEmpty()) {
            googleLog.error("Request body is null!")
            return
        }

        var googleSubscriptionEventDto: GoogleSubscriptionEventDto? = null

        var error: Exception? = null
        var dataStringDecoded: String? = null

        try {
            googleSubscriptionEventDto = objectMapper.readValue(body, GoogleSubscriptionEventDto::class.java)

            dataStringDecoded = Base64Utils
                .decodeFromString(googleSubscriptionEventDto.message!!.data!!)
                .toString(Charsets.UTF_8)
            val parsedRequest = objectMapper.readValue(dataStringDecoded, DeveloperNotification::class.java)
            googleLog.info(
                "parsedRequest: ${
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsedRequest)
                }"
            )
            checkNotNull(parsedRequest.subscriptionNotification)
            when (parsedRequest.subscriptionNotification.notificationTypeEnum) {
                SUBSCRIPTION_PURCHASED,
                SUBSCRIPTION_DEFERRED,
                SUBSCRIPTION_PRICE_CHANGE_CONFIRMED,
                SUBSCRIPTION_ON_HOLD,
                SUBSCRIPTION_IN_GRACE_PERIOD -> {
                    //nothing to do.
                }

                //send push with APPLIED message
                SUBSCRIPTION_RENEWED, SUBSCRIPTION_RESTARTED, SUBSCRIPTION_RECOVERED -> {
                    val inAppPurchaseData = parsedRequest.subscriptionNotification
                    val subId = inAppPurchaseData.subscriptionId
                    val purchaseToken = inAppPurchaseData.purchaseToken

                    val subscriptionInDb = googleSubscriptionService
                        .findAllByPurchaseToken(inAppPurchaseData.purchaseToken)
                        .maxByOrNull { it.orderId }
                        ?: throw GoogleSubscriptionNotFoundException(
                            "GoogleSubscription not found for purchaseToken: $purchaseToken"
                        )
                    val user: User = googleSubscriptionService
                        .getUserByGoogleSubscriptionId(subscriptionInDb.id!!) ?: throw UserNotFoundException()

                    val updatedUser = applyGoogleSubscription(
                        false,
                        subId,
                        inAppPurchaseData.purchaseToken,
                        user,
                        pushTitle = "Subscription renewal!",
                        pushMessage = "Your subscription was successfully renewed!"
                    )
                    val updatedSubscription = googleSubscriptionService
                        .getById(subscriptionInDb.id)
                    googleLog.info(
                        """
                        Successfully update renewed google subscription (SUBSCRIPTION_RENEWED, SUBSCRIPTION_RESTARTED, SUBSCRIPTION_RECOVERED)!
                        User: ${updatedUser.id}/${updatedUser.offlineLimitDisabledEndDate}.
                        Subscription: ${subscriptionInDb.id}/${updatedSubscription?.expiryTimeMillis}.
                    """.trimIndent()
                    )
                }
                //send push with EXPIRED message
                SUBSCRIPTION_PAUSED, SUBSCRIPTION_REVOKED, SUBSCRIPTION_EXPIRED -> {
                    val inAppPurchaseData = parsedRequest.subscriptionNotification
                    val subId = inAppPurchaseData.subscriptionId
                    val purchaseToken = inAppPurchaseData.purchaseToken

                    val subscriptionInDb = googleSubscriptionService
                        .findAllByPurchaseToken(inAppPurchaseData.purchaseToken)
                        .maxByOrNull { it.orderId }
                        ?: throw GoogleSubscriptionNotFoundException(
                            "GoogleSubscription not found for purchaseToken: $purchaseToken"
                        )
                    val user: User = googleSubscriptionService
                        .getUserByGoogleSubscriptionId(subscriptionInDb.id!!) ?: throw UserNotFoundException()

                    val updatedUser = applyGoogleSubscription(
                        false,
                        subId,
                        inAppPurchaseData.purchaseToken,
                        user,
                        pushTitle = "Subscription expired.",
                        pushMessage = "Subscription has expired and no longer active."
                    )
                    val updatedSubscription = googleSubscriptionService
                        .getById(subscriptionInDb.id)
                    googleLog.info(
                        """
                        Successfully update renewed google subscription (SUBSCRIPTION_RENEWED, SUBSCRIPTION_RESTARTED, SUBSCRIPTION_RECOVERED)!
                        User: ${updatedUser.id}/${updatedUser.offlineLimitDisabledEndDate}.
                        Subscription: ${subscriptionInDb.id}/${updatedSubscription?.expiryTimeMillis}.
                    """.trimIndent()
                    )
                }
                SUBSCRIPTION_PAUSE_SCHEDULE_CHANGED -> {
                    //nothing to do?..
                }
                SUBSCRIPTION_CANCELED -> {
                    //nothing to do? As we'll receive expired event...
                }
            }
        } catch (e: Exception) {
            error = e
            googleLog.error("Error while handle google subscription event", error)
        } finally {
            googleLog.info("write google attempt to DB START")
            @Suppress("DuplicatedCode")
            googleSubsEventHandleAttemptService.save(
                GoogleSubscriptionEventHandleAttemptRecord(
                    decodedDataJson = dataStringDecoded ?: "",
                    encodedData = googleSubscriptionEventDto?.message?.data ?: "googleSubscriptionEventDto is NULL!",
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
            googleLog.info("write google attempt to DB END")
        }
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
            huaweiLog.info(
                "parsedRequest: ${
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsedRequest)
                }"
            )

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
                    huaweiLog.info(
                        """
                        Successfully update renewed Huawei subscription (RENEWAL, RENEWAL_RESTORED, RENEWAL_RECURRING)!
                        User: ${updatedUser.id}/${updatedUser.offlineLimitDisabledEndDate}. 
                        Subscription: ${huaweiSubscriptionInDb.id}/${updatedSubscription?.expiryTimeMillis}.
                    """.trimIndent()
                    )
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
                    huaweiLog.info(
                        """
                        Successfully update renewed Huawei subscription (INTERACTIVE_RENEWAL)!
                        User: ${updatedUser.id}/${updatedUser.offlineLimitDisabledEndDate}. 
                        Subscription: ${huaweiSubscriptionInDb.id}/${updatedSubscription?.expiryTimeMillis}.
                    """.trimIndent()
                    )
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
                    huaweiLog.info(
                        """
                        Successfully save changed Huawei subscription (NEW_RENEWAL_PREF)!
                        User: ${updatedUser.id}/${updatedUser.offlineLimitDisabledEndDate}. 
                        Subscription: ${huaweiSubscriptionInDb.id}/${updatedSubscription?.expiryTimeMillis}.
                    """.trimIndent()
                    )
                }
            }
        } catch (e: Exception) {
            error = e
            huaweiLog.error("Error while handle huawei subscription event", error)
        } finally {
            @Suppress("DuplicatedCode")
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
        @RequestParam subscriptionId: String?,
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
                            subscriptionId = subscriptionId!!,
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
            Store.GOOGLE -> {
                when (purchaseType) {
                    InappType.SUBS -> {
                        return applyGoogleSubscription(
                            acknowledge = true,
                            subscriptionId = productId,
                            purchaseToken = purchaseToken,
                            user = user,
                            pushTitle = "Subscription is active!",
                            pushMessage = "Your subscription was successfully activated!"
                        )
                    }
                    InappType.INAPP, InappType.CONSUMABLE -> TODO()
                }
            }
            Store.AMAZON -> TODO()
            Store.APPLE -> TODO()
        }
    }

    /**
     * @param subscriptionId is SKU, i.e. "month_01"
     *
     * @throws GooglePurchaseError
     */
    private fun applyGoogleSubscription(
        acknowledge: Boolean,
        subscriptionId: String,
        purchaseToken: String,
        user: User,
        pushTitle: String,
        pushMessage: String
    ): UserProjectionV2 {
        googleLog.info("Start applying google subscription: $subscriptionId, $purchaseToken, ${user.id}")
        checkNotNull(user.id)
        // 1. Verify product
        val verificationResult = googlePurchaseService.validateSubscriptionPurchase(
            packageName = googlePackageName,
            purchaseToken = purchaseToken,
            sku = subscriptionId
        ) as? ValidationResponse.GoogleSubscriptionResponse?
            ?: throw GooglePurchaseError("Error in google subs validation!", IllegalStateException())
        googleLog.info("verificationResult: $verificationResult")

        val googleSubscription: SubscriptionPurchase = when (verificationResult.status) {
            ValidationStatus.VALID -> verificationResult.googleSubscription!!
            ValidationStatus.INVALID -> throw GooglePurchaseError(
                "Subscription is not valid!.",
                IllegalStateException()
            )
            ValidationStatus.SERVER_ERROR -> throw GooglePurchaseError(
                "Can't validate subscription as google api is down.",
                IllegalStateException()
            )
        }

        //2. Write product info to DB.
        val subscriptionInDb = googleSubscriptionService.saveSubscription(
            googleSubscription,
            purchaseToken,
            subscriptionId,
            user
        )
        googleLog.info("subscriptionInDb: $subscriptionInDb")

        //3. Update user in DB.
        updateUserSubscriptionExpiration(user.id)

        //4. Acknowledge product if need
        if (acknowledge) {
            val acknowledgeResult = googlePurchaseService.acknowledgeSubscription(
                subscriptionId = subscriptionId, purchaseToken = purchaseToken
            )
            if (acknowledgeResult.success) {
                googleLog.info("Subscription acknowledge is successful!")
            } else {
                check(acknowledgeResult is GoogleAcknowledgeResult.GoogleSubscriptionAcknowledgeFailure)
                throw GooglePurchaseError("Error while acknowledge subscription!", acknowledgeResult.cause)
            }
        }

        //5. Send push
        allProvidersMessagingService.sendToUser(
            userId = user.id,
            title = pushTitle,
            message = pushMessage,
            type = ScpReaderConstants.Push.MessageType.SUBSCRIPTION_EVENT,
            author = userService.getById(ADMIN_ID)
                ?: throw UserNotFoundException("Can't find admin user with id: $ADMIN_ID")
        )

        //6. Return updated user
        return userService.getByIdAsDtoV2(user.id) ?: throw UserNotFoundException()
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
            author = userService.getById(ADMIN_ID)
                ?: throw UserNotFoundException("Can't find admin user with id: $ADMIN_ID")
        )

        //5. Return updated user
        return userService.getByIdAsDtoV2(user.id) ?: throw UserNotFoundException()
    }

    private fun updateUserSubscriptionExpiration(userId: Long): User {
        val userInDb = userService.getById(userId) ?: throw UserNotFoundException()

        val curTimeMillis = Instant.now().toEpochMilli()
        val userHuaweiSubs = huaweiMonetizationService
            .getHuaweiSubscriptionsForUser(userId)
        val userNonExpiredAndValidHuaweiSubscriptions = userHuaweiSubs
            .filter { it.subIsValid }
            .filter { it.expiryTimeMillis!!.toInstant(ZoneOffset.UTC).toEpochMilli() > curTimeMillis }
            .sortedBy { it.expiryTimeMillis }

        val userGoogleSubs = googleSubscriptionService
            .getGoogleSubscriptionsForUser(userId)
        val userNonExpiredGoogleSubscriptions = userGoogleSubs
            .filter { it.expiryTimeMillis!!.toInstant(ZoneOffset.UTC).toEpochMilli() > curTimeMillis }
            .sortedBy { it.expiryTimeMillis }

        val hasNoValidSubscription = userNonExpiredAndValidHuaweiSubscriptions.isEmpty() ||
                userNonExpiredGoogleSubscriptions.isEmpty()

        return if (hasNoValidSubscription) {
            googleLog.error("User have no valid and nonExpired subscription.")
            huaweiLog.error("User have no valid and nonExpired subscription.")

            //so check subscriptions expire times, update it in DB and print in logs if we have smth strange

            val maxExpiredSubsTime: List<LocalDateTime> = listOfNotNull(
                userHuaweiSubs.maxByOrNull { it.expiryTimeMillis!! }?.expiryTimeMillis,
                userGoogleSubs.maxByOrNull { it.expiryTimeMillis!! }?.expiryTimeMillis
            )
                .sortedWith(Comparator.reverseOrder())

            if (maxExpiredSubsTime.isEmpty()) {
                userInDb
            } else {
                val maxExpireTime = maxExpiredSubsTime.first()

                userService.update(
                    userInDb.apply { offlineLimitDisabledEndDate = maxExpireTime }
                )
            }
        } else {
            val maxExpireTimeHuaweiSub = userNonExpiredAndValidHuaweiSubscriptions.firstOrNull()
            val maxExpireTimeGoogleSub = userNonExpiredGoogleSubscriptions.firstOrNull()
            val expireTimesList = listOfNotNull(
                maxExpireTimeGoogleSub?.expiryTimeMillis,
                maxExpireTimeHuaweiSub?.expiryTimeMillis
            )
                .sortedWith(Comparator.reverseOrder())

            val maxExpireTime = expireTimesList.first()
            huaweiLog.info("userNonExpiredAndValidSubscriptions max expiryTimeMillis: $maxExpireTime")
            googleLog.info("userNonExpiredAndValidSubscriptions max expiryTimeMillis: $maxExpireTime")
            userService.update(
                userInDb.apply { offlineLimitDisabledEndDate = maxExpireTime }
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

        val googleSubscriptions = if (showAll) {
            googleSubscriptionService
                .getGoogleSubscriptionsForUser(user.id)
        } else {
            val curTimeMillis = Instant.now().toEpochMilli()
            googleSubscriptionService
                .getGoogleSubscriptionsForUser(user.id)
                .filter { it.expiryTimeMillis!!.toInstant(ZoneOffset.UTC).toEpochMilli() > curTimeMillis }
        }
            .sortedBy { it.expiryTimeMillis }

        return UserSubscriptionsDto(
            huaweiSubscriptions = huaweiSubscriptions,
            googleSubscriptions = googleSubscriptions
        )
    }

    @GetMapping("/cancel/{store}/{purchaseType}")
    fun cancelProduct(
        @PathVariable store: Store,
        @PathVariable purchaseType: InappType,
        @RequestParam productId: String,
        @RequestParam purchaseToken: String,
        @RequestParam(defaultValue = ACCOUNT_FLAG_HUAWEI_ID.toString()) accountFlag: Int
    ): String {
        return when (store) {
            Store.HUAWEI -> huaweiApiService.cancelSubscription(productId, purchaseToken, accountFlag).toString()
            Store.GOOGLE -> {
                "This can be done in console. Use it, please"
            }
            Store.AMAZON -> {
                "Seems to be we do not need it"
            }
            Store.APPLE -> {
                "Seems to be we do not need it"
            }
        }
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
        val updatedSubscriptions: List<HuaweiSubscription> =
            recentlyExpiredSubscriptions.mapNotNull { currentSubscription ->
                //Do not try to validate after 3 days of unsuccessful attempts if period is not Period.WEEK
                val previousAttempts = subscriptionValidateAttemptsService
                    .getByStoreAndSubscriptionId(Store.HUAWEI.name, currentSubscription.id!!)
                if (
                    period != Period.WEEK
                    && previousAttempts != null
                    && previousAttempts.attempts >= MAX_VALIDATION_ATTEMPTS
                ) {
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

                val huaweiSubscriptionResponse =
                    (verificationResult as ValidationResponse.HuaweiSubscriptionResponse)

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
                    author = userService.getById(ADMIN_ID)
                        ?: throw UserNotFoundException()
                )

                updatedSubscription

            }

        return updatedSubscriptions
    }
}
