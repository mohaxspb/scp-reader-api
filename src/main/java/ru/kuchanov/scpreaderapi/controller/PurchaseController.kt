package ru.kuchanov.scpreaderapi.controller

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.purchase.PurchaseAndroidService


@RestController
@RequestMapping("/${ScpReaderConstants.Path.PURCHASE}")
class PurchaseController {

    @Autowired
    private lateinit var log: Logger

    @Autowired
    private lateinit var purchaseAndroidService: PurchaseAndroidService


    @GetMapping("/validateAndroidProduct")
    fun validateAndroidProduct(
            @RequestParam(value = "package") androidPackage: String,
            @RequestParam(value = "sku") sku: String,
            @RequestParam(value = "token") token: String,
            @AuthenticationPrincipal user: User?
    ) = purchaseAndroidService.validateProductPurchase(
            packageName = androidPackage,
            sku = sku,
            purchaseToken = token
    )

    @GetMapping("/validateAndroidSubscription")
    fun validateAndroidSubscription(
            @RequestParam(value = "package") androidPackage: String,
            @RequestParam(value = "sku") sku: String,
            @RequestParam(value = "token") token: String,
            @AuthenticationPrincipal user: User?
    ) = purchaseAndroidService.validateSubscriptionPurchase(
            packageName = androidPackage,
            sku = sku,
            purchaseToken = token
    )
}