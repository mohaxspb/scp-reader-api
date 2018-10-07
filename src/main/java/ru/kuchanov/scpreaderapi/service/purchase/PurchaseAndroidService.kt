package ru.kuchanov.scpreaderapi.service.purchase

import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse

interface PurchaseAndroidService {

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
}