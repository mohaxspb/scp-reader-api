package ru.kuchanov.scpreaderapi.model.dto.purchase

import com.fasterxml.jackson.annotation.JsonValue
import com.google.api.services.androidpublisher.model.ProductPurchase
import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import ru.kuchanov.scpreaderapi.model.monetization.huawei.InAppPurchaseData

enum class ValidationStatus {

    VALID, INVALID, SERVER_ERROR;

    @JsonValue
    fun serializedValue() = ordinal
}

sealed class ValidationResponse(open val status: ValidationStatus) {
    data class AndroidProductResponse(
            override val status: ValidationStatus,
            val androidProduct: ProductPurchase?
    ) : ValidationResponse(status)

    data class AndroidSubscriptionResponse(
            override val status: ValidationStatus,
            val androidSubscription: SubscriptionPurchase?
    ) : ValidationResponse(status)

    data class HuaweiSubscriptionResponse(
            override val status: ValidationStatus,
            val androidSubscription: InAppPurchaseData?
    ) : ValidationResponse(status)
}