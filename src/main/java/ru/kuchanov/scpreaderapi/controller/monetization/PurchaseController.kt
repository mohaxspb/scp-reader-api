package ru.kuchanov.scpreaderapi.controller.monetization

import com.google.api.services.androidpublisher.model.ProductPurchase
import com.google.api.services.androidpublisher.model.SubscriptionPurchase
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
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationStatus
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei.HuaweiService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.AndroidProductService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google.AndroidPurchaseService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.AndroidSubscriptionService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.UserAndroidPurchaseService
import java.sql.Timestamp


//todo refactor for amazon/apple/google/huawei
@RestController
@RequestMapping("/" + ScpReaderConstants.Path.MONETIZATION + "/" + ScpReaderConstants.Path.PURCHASE)
class PurchaseController @Autowired constructor(
        val huaweiService: HuaweiService,
        val androidPurchaseService: AndroidPurchaseService,
        val androidProductService: AndroidProductService,
        val androidSubscriptionService: AndroidSubscriptionService,
        val userAndroidPurchaseService: UserAndroidPurchaseService
) {

    @GetMapping("/cancel/{store}/{purchaseType}")
    fun cancelProduct(
            @PathVariable store: Store,
            @RequestParam productId: String,
            @RequestParam purchaseToken: String,
            @RequestParam purchaseType: InappType,
            @RequestParam(defaultValue = "-1") accountFlag: Int
    ): String {
        val test = when (store) {
            Store.HUAWEI -> huaweiService.cancelSubscription(productId, purchaseToken, accountFlag)
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
            Store.HUAWEI -> huaweiService.verifyProduct(productId, subscriptionId, purchaseType, purchaseToken, accountFlag)
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
    ) {
        // 1. Verify product
        val verificationResult = when (store) {
            Store.HUAWEI -> huaweiService.verifyProduct(productId, subscriptionId, purchaseType, purchaseToken, accountFlag)
            Store.GOOGLE -> TODO()
            Store.AMAZON -> TODO()
            Store.APPLE -> TODO()
        }
        if (verificationResult.status == ValidationStatus.VALID) {
            val huaweiSubscriptionResponse =
                    (verificationResult as ValidationResponse.HuaweiSubscriptionResponse)
            huaweiService.savePurchasedProduct(huaweiSubscriptionResponse.androidSubscription!!)
        }
        //TODO 2. Write product info to DB.
        //TODO 2.1. Check if user already has subscriptions
        //TODO 3. Update user in DB.
    }

    @GetMapping("/subscription/all")
    fun getUserSubscriptions(
            @AuthenticationPrincipal user: User
    ) {
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
