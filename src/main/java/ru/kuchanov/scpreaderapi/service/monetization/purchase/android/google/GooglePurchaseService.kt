package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google

import ru.kuchanov.scpreaderapi.model.dto.purchase.GoogleAcknowledgeResult
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse

interface GooglePurchaseService {

    fun validateProductPurchase(
            packageName: String,
            sku: String,
            purchaseToken: String
    ): ValidationResponse

    fun validateSubscriptionPurchase(
            packageName: String,
            sku: String,
            purchaseToken: String
    ): ValidationResponse

    fun acknowledgeSubscription(
        subscriptionId: String,
        purchaseToken: String
    ): GoogleAcknowledgeResult
}