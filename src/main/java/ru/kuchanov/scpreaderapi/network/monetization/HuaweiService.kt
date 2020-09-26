package ru.kuchanov.scpreaderapi.network.monetization

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.bean.monetization.InappType
import ru.kuchanov.scpreaderapi.configuration.monetization.HuaweiPurchaseConfiguration
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationStatus
import ru.kuchanov.scpreaderapi.model.monetization.huawei.HuaweiProductVerifyResponse
import ru.kuchanov.scpreaderapi.network.HuaweiApi

@Service
class HuaweiService @Autowired constructor(
        @Qualifier(HuaweiPurchaseConfiguration.QUALIFIER_SUBS_GERMANY_APP_TOUCH)
        private val huaweiApiSubsGermany: HuaweiApi,
        @Qualifier(HuaweiPurchaseConfiguration.QUALIFIER_ORDER_GERMANY_APP_TOUCH)
        private val huaweiApiOrderGermany: HuaweiApi,
        @Qualifier(HuaweiPurchaseConfiguration.QUALIFIER_SUBS_RUSSIA)
        private val huaweiApiSubsRussia: HuaweiApi,
        @Qualifier(HuaweiPurchaseConfiguration.QUALIFIER_ORDER_RUSSIA)
        private val huaweiApiOrderRussia: HuaweiApi
) {

    companion object {
        const val ACCOUNT_FLAG_GERMANY_APP_TOUCH = 1
    }

    fun verifyProduct(
            productId: String,
            purchaseType: InappType,
            purchaseToken: String,
            accountFlag: Int
    ): ValidationResponse {
        return when (purchaseType) {
            InappType.INAPP, InappType.CONSUMABLE -> TODO()
            InappType.SUBS -> {
                val result = verifySubscription(productId, purchaseToken, accountFlag)
                if (result.responseCode == 0) {
                    ValidationResponse.HuaweiSubscriptionResponse(ValidationStatus.VALID, result)
                } else {
                    ValidationResponse.HuaweiSubscriptionResponse(ValidationStatus.INVALID, result)
                }
            }
        }
    }

    fun verifySubscription(
            productId: String,
            purchaseToken: String,
            accountFlag: Int
    ): HuaweiProductVerifyResponse {
        val api = if (accountFlag == ACCOUNT_FLAG_GERMANY_APP_TOUCH) {
            huaweiApiSubsGermany
        } else {
            huaweiApiSubsRussia
        }

        return try {
            api.verifySubscription(productId, purchaseToken).execute().body()!!
        } catch (e: Throwable) {
            throw VerifyProductException("Cannot verify subscription with error message: ${e.message}.", e)
        }
    }
}

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class VerifyProductException(
        override val message: String?,
        override val cause: Throwable?
) : RuntimeException(message, cause)
