package ru.kuchanov.scpreaderapi.model.dto.purchase

import com.fasterxml.jackson.annotation.JsonValue
import com.google.api.services.androidpublisher.model.ProductPurchase
import com.google.api.services.androidpublisher.model.SubscriptionPurchase

enum class ValidationStatus {
    VALID, INVALID, GOOGLE_SERVER_ERROR;

    @JsonValue
    fun serializedValue() = ordinal
}

open class ValidationResponse(open val status: ValidationStatus)

data class AndroidProductResponse(
        override val status: ValidationStatus,
        val androidProduct: ProductPurchase
) : ValidationResponse(status)

data class AndroidSubscriptionResponse(
        override val status: ValidationStatus,
        val androidSubscription: SubscriptionPurchase
) : ValidationResponse(status)