package ru.kuchanov.scpreaderapi.controller.monetization

import com.google.api.services.androidpublisher.model.ProductPurchase
import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.monetization.InappType
import ru.kuchanov.scpreaderapi.bean.monetization.Store
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidSubscription
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidSubscription
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationStatus
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjection
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.AndroidProductService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.AndroidSubscriptionService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.UserAndroidPurchaseService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google.AndroidPurchaseService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiApiService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiMonetizationService
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserService
import java.sql.Timestamp
import java.time.Instant
import java.time.ZoneOffset


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.MONETIZATION + "/" + ScpReaderConstants.Path.PURCHASE)
class PurchaseController @Autowired constructor(
        private val huaweiApiService: HuaweiApiService,
        private val huaweiMonetizationService: HuaweiMonetizationService,
        private val androidPurchaseService: AndroidPurchaseService,
        private val androidProductService: AndroidProductService,
        private val androidSubscriptionService: AndroidSubscriptionService,
        private val userAndroidPurchaseService: UserAndroidPurchaseService,
        private val userService: ScpReaderUserService,
        private val log: Logger
) {

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

    @GetMapping("/verify/{store}/{purchaseType}")
    fun verifyAndroidProduct(
            @PathVariable store: Store,
            @PathVariable purchaseType: InappType,
            @RequestParam productId: String,
            @RequestParam subscriptionId: String,
            @RequestParam purchaseToken: String,
            @RequestParam(defaultValue = "-1") accountFlag: Int,
            @AuthenticationPrincipal user: User?
    ): ValidationResponse {
        return when (store) {
            Store.HUAWEI -> huaweiApiService.verifyProduct(productId, subscriptionId, purchaseType, purchaseToken, accountFlag)
            Store.GOOGLE -> TODO()
            Store.AMAZON -> TODO()
            Store.APPLE -> TODO()
        }
    }

    @PostMapping("/apply/{store}/{purchaseType}")
    fun applyAndroidProduct(
            @PathVariable store: Store,
            @PathVariable purchaseType: InappType,
            @RequestParam productId: String,
            @RequestParam subscriptionId: String,
            @RequestParam purchaseToken: String,
            @RequestParam(defaultValue = "-1") accountFlag: Int,
            @AuthenticationPrincipal user: User?
    ): UserProjection {
        check(user != null) { "User is null!" }
        check(user.id != null) { "User ID is null!" }
        // 1. Verify product
        val verificationResult = when (store) {
            Store.HUAWEI -> huaweiApiService.verifyProduct(productId, subscriptionId, purchaseType, purchaseToken, accountFlag)
            Store.GOOGLE -> TODO()
            Store.AMAZON -> TODO()
            Store.APPLE -> TODO()
        }
        if (verificationResult.status == ValidationStatus.VALID) {
            val huaweiSubscriptionResponse =
                    (verificationResult as ValidationResponse.HuaweiSubscriptionResponse)
            //2. Write product info to DB.
            huaweiMonetizationService.savePurchasedProduct(huaweiSubscriptionResponse.androidSubscription!!, user)
        }
        //3. Update user in DB.

        when (purchaseType) {
            InappType.INAPP, InappType.CONSUMABLE -> TODO()
            InappType.SUBS -> {
                val curTimeMillis = Instant.now().toEpochMilli()
                val userNonExpiredAndValidSubscriptions = huaweiMonetizationService
                        .getHuaweiSubscriptionsForUser(user.id)
                        .filter { it.subIsValid }
                        .filter { it.expiryTimeMillis!!.toInstant(ZoneOffset.UTC).toEpochMilli() > curTimeMillis }
                        .sortedBy { it.expiryTimeMillis }
                log.error("userNonExpiredAndValidSubscriptions: ${userNonExpiredAndValidSubscriptions.size}")
                val maxExpireTimeSub = userNonExpiredAndValidSubscriptions.first()
                log.error("userNonExpiredAndValidSubscriptions max expiryTimeMillis: ${maxExpireTimeSub.expiryTimeMillis}")

                val userInDb = userService.getById(user.id) ?: throw UserNotFoundException()
                userService.save(
                        userInDb.apply {
                            offlineLimitDisabledEndDate = maxExpireTimeSub.expiryTimeMillis
                        }
                )

                return userService.getByIdAsDto(user.id) ?: throw UserNotFoundException()
            }
        }
    }

    @GetMapping("/subscription/all")
    fun getUserSubscriptions(@AuthenticationPrincipal user: User) {
        TODO()
    }

    @GetMapping("/validateAndroidProduct")
    fun validateAndroidProduct(
            @RequestParam(value = "package") androidPackage: String,
            @RequestParam(value = "sku") sku: String,
            @RequestParam(value = "token") token: String,
            @AuthenticationPrincipal user: User?
    ): ValidationResponse {
        val productResponse = androidPurchaseService.validateProductPurchase(
                packageName = androidPackage,
                sku = sku,
                purchaseToken = token
        ) as ValidationResponse.AndroidProductResponse

        if (productResponse.status == ValidationStatus.VALID) {
            val product: ProductPurchase = productResponse.androidProduct!!

            var androidProduct = androidProductService.getByPurchaseToken(token)

            if (androidProduct == null) {
                androidProduct = androidProductService.save(AndroidProduct(
                        purchaseToken = token,
                        androidPackage = androidPackage,
                        orderId = product.orderId,
                        purchaseState = product.purchaseState,
                        consumptionState = product.consumptionState,
                        purchaseTimeMillis = Timestamp(product.purchaseTimeMillis),
                        purchaseType = product.purchaseType
                ))
            } else {
                androidProduct.purchaseState = product.purchaseState
                androidProduct.consumptionState = product.consumptionState
                androidProductService.save(androidProduct)
            }

            user?.let {
                if (userAndroidPurchaseService.getByUserIdAndAndroidProductId(user.id!!, androidProduct.id!!) == null) {
                    userAndroidPurchaseService.save(UsersAndroidProduct(
                            userId = user.id,
                            androidProductId = androidProduct.id!!
                    ))
                }
            }
        }

        return productResponse
    }

    @GetMapping("/validateAndroidSubscription")
    fun validateAndroidSubscription(
            @RequestParam(value = "package") androidPackage: String,
            @RequestParam(value = "sku") sku: String,
            @RequestParam(value = "token") token: String,
            @AuthenticationPrincipal user: User?
    ): ValidationResponse {
        val subscriptionResponse = androidPurchaseService.validateSubscriptionPurchase(
                packageName = androidPackage,
                sku = sku,
                purchaseToken = token
        ) as ValidationResponse.AndroidSubscriptionResponse

        if (subscriptionResponse.status == ValidationStatus.VALID) {
            val subscription: SubscriptionPurchase = subscriptionResponse.androidSubscription!!

            var androidSubscription = androidSubscriptionService.getByPurchaseToken(token)

            if (androidSubscription == null) {
                androidSubscription = androidSubscriptionService.save(AndroidSubscription(
                        orderId = subscription.orderId,
                        purchaseToken = token,
                        androidPackage = androidPackage,
                        linkedPurchaseToken = subscription.linkedPurchaseToken,
                        priceAmountMicros = subscription.priceAmountMicros,
                        priceCurrencyCode = subscription.priceCurrencyCode,
                        autoRenewing = subscription.autoRenewing,
                        startTimeMillis = Timestamp(subscription.startTimeMillis),
                        expiryTimeMillis = Timestamp(subscription.expiryTimeMillis),
                        userCancellationTimeMillis = subscription.userCancellationTimeMillis?.let { Timestamp(it) }
                ))
            } else {
                androidSubscription.orderId = subscription.orderId
                androidSubscription.linkedPurchaseToken = subscription.linkedPurchaseToken
                androidSubscription.startTimeMillis = Timestamp(subscription.startTimeMillis)
                androidSubscription.expiryTimeMillis = Timestamp(subscription.expiryTimeMillis)
                androidSubscription.userCancellationTimeMillis = subscription.userCancellationTimeMillis?.let { Timestamp(it) }
                androidSubscriptionService.save(androidSubscription)
            }

            user?.let {
                if (userAndroidPurchaseService.getByUserIdAndAndroidSubscriptionId(user.id!!, androidSubscription.id!!) == null) {
                    userAndroidPurchaseService.save(UsersAndroidSubscription(
                            userId = user.id,
                            androidSubscriptionId = androidSubscription.id!!
                    ))
                }
            }
        }

        return subscriptionResponse
    }
}
