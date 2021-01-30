package ru.kuchanov.scpreaderapi.controller.monetization

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.monetization.InappType
import ru.kuchanov.scpreaderapi.bean.monetization.Store
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscription
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.model.dto.monetization.UserSubscriptionsDto
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjection
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiApiService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiApiService.Companion.ACCOUNT_FLAG_GERMANY_APP_TOUCH
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiMonetizationService
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserService
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.MONETIZATION + "/" + ScpReaderConstants.Path.PURCHASE)
class PurchaseController @Autowired constructor(
        private val huaweiApiService: HuaweiApiService,
        private val huaweiMonetizationService: HuaweiMonetizationService,
        private val userService: ScpReaderUserService,
        private val log: Logger
) {

    @PostMapping("/apply/{store}/{purchaseType}")
    fun applyAndroidProduct(
            @PathVariable store: Store,
            @PathVariable purchaseType: InappType,
            @RequestParam productId: String,
            @RequestParam subscriptionId: String,
            @RequestParam purchaseToken: String,
            @RequestParam(defaultValue = ACCOUNT_FLAG_GERMANY_APP_TOUCH.toString()) accountFlag: Int,
            @AuthenticationPrincipal user: User?
    ): UserProjection {
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

                return userService.getByIdAsDto(user.id) ?: throw UserNotFoundException()
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
        val maxExpireTimeSub = userNonExpiredAndValidSubscriptions.first()
        log.error("userNonExpiredAndValidSubscriptions max expiryTimeMillis: ${maxExpireTimeSub.expiryTimeMillis}")
        return userService.update(
                userInDb.apply { offlineLimitDisabledEndDate = maxExpireTimeSub.expiryTimeMillis }
        )
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
}
