package ru.kuchanov.scpreaderapi.controller

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
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.dto.purchase.AndroidProductResponse
import ru.kuchanov.scpreaderapi.model.dto.purchase.AndroidSubscriptionResponse
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse
import ru.kuchanov.scpreaderapi.service.purchase.AndroidProductService
import ru.kuchanov.scpreaderapi.service.purchase.AndroidSubscriptionService
import ru.kuchanov.scpreaderapi.service.purchase.PurchaseAndroidService
import java.sql.Timestamp


@RestController
@RequestMapping("/${ScpReaderConstants.Path.PURCHASE}")
class PurchaseController {

    @Autowired
    private lateinit var log: Logger

    @Autowired
    private lateinit var purchaseAndroidService: PurchaseAndroidService

    @Autowired
    private lateinit var androidProductService: AndroidProductService

    @Autowired
    private lateinit var androidSubscriptionService: AndroidSubscriptionService

    @GetMapping("/validateAndroidProduct")
    fun validateAndroidProduct(
            @RequestParam(value = "package") androidPackage: String,
            @RequestParam(value = "sku") sku: String,
            @RequestParam(value = "token") token: String,
            @AuthenticationPrincipal user: User?
    ): ValidationResponse {
        val productResponse = purchaseAndroidService.validateProductPurchase(
                packageName = androidPackage,
                sku = sku,
                purchaseToken = token
        ) as AndroidProductResponse

        val product = productResponse.androidProduct
        androidProductService.save(AndroidProduct(
                purchaseToken = token,
                orderId = product.orderId,
                purchaseState = product.purchaseState,
                consumptionState = product.consumptionState,
                purchaseTimeMillis = Timestamp(product.purchaseTimeMillis),
                userId = user?.id
        ))

        return ValidationResponse(productResponse.status)
    }

    @GetMapping("/validateAndroidSubscription")
    fun validateAndroidSubscription(
            @RequestParam(value = "package") androidPackage: String,
            @RequestParam(value = "sku") sku: String,
            @RequestParam(value = "token") token: String,
            @AuthenticationPrincipal user: User?
    ): ValidationResponse {
        val subscriptionResponse = purchaseAndroidService.validateSubscriptionPurchase(
                packageName = androidPackage,
                sku = sku,
                purchaseToken = token
        ) as AndroidSubscriptionResponse

        val subscription = subscriptionResponse.androidSubscription
        androidSubscriptionService.save(AndroidSubscription(
                orderId = subscription.orderId,
                purchaseToken = token,
                linkedPurchaseToken = subscription.linkedPurchaseToken,
                priceAmountMicros = subscription.priceAmountMicros,
                priceCurrencyCode = subscription.priceCurrencyCode,
                autoRenewing = subscription.autoRenewing,
                startTimeMillis = Timestamp(subscription.startTimeMillis),
                expiryTimeMillis = Timestamp(subscription.expiryTimeMillis),
                userCancellationTimeMillis = Timestamp(subscription.userCancellationTimeMillis),
                userId = user?.id
        ))

        return ValidationResponse(subscriptionResponse.status)
    }
}