package ru.kuchanov.scpreaderapi.service.monetization.purchase.android

import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse

interface AndroidPurchaseService {

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