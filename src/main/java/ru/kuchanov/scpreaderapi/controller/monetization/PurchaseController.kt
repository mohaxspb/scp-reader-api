package ru.kuchanov.scpreaderapi.controller.monetization

import com.google.api.services.androidpublisher.model.ProductPurchase
import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidSubscription
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidSubscription
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.dto.purchase.AndroidProductResponse
import ru.kuchanov.scpreaderapi.model.dto.purchase.AndroidSubscriptionResponse
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationStatus
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.AndroidProductService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.AndroidPurchaseService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.AndroidSubscriptionService
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.UserAndroidPurchaseService
import java.sql.Timestamp


@RestController
@RequestMapping("/${ScpReaderConstants.Path.PURCHASE}")
class PurchaseController {

    @Autowired
    private lateinit var log: Logger

    @Autowired
    private lateinit var androidPurchaseService: AndroidPurchaseService

    @Autowired
    private lateinit var androidProductService: AndroidProductService

    @Autowired
    private lateinit var androidSubscriptionService: AndroidSubscriptionService

    @Autowired
    private lateinit var userAndroidPurchaseService: UserAndroidPurchaseService

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
        ) as AndroidProductResponse

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

        return ValidationResponse(productResponse.status)
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
        ) as AndroidSubscriptionResponse

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

        return ValidationResponse(subscriptionResponse.status)
    }

    @GetMapping("/android/subscription/all")
    fun showAndroidSubscriptions() = androidSubscriptionService.findAll()

    @GetMapping("/android/product/all")
    fun showAndroidProducts() = androidProductService.findAll()
}