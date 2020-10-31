package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.bean.monetization.InappType
import ru.kuchanov.scpreaderapi.configuration.monetization.HuaweiPurchaseConfiguration
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationStatus
import ru.kuchanov.scpreaderapi.model.huawei.purchase.InAppPurchaseData
import ru.kuchanov.scpreaderapi.model.monetization.huawei.HuaweiProductVerifyResponse
import ru.kuchanov.scpreaderapi.model.monetization.huawei.HuaweiSubscriptionCancelResponse
import ru.kuchanov.scpreaderapi.network.HuaweiApi

@Service
class HuaweiApiService @Autowired constructor(
        @Qualifier(HuaweiPurchaseConfiguration.QUALIFIER_SUBS_GERMANY_APP_TOUCH)
        private val huaweiApiSubsGermany: HuaweiApi,
        @Qualifier(HuaweiPurchaseConfiguration.QUALIFIER_ORDER_GERMANY_APP_TOUCH)
        private val huaweiApiOrderGermany: HuaweiApi,
        @Qualifier(HuaweiPurchaseConfiguration.QUALIFIER_SUBS_RUSSIA)
        private val huaweiApiSubsRussia: HuaweiApi,
        @Qualifier(HuaweiPurchaseConfiguration.QUALIFIER_ORDER_RUSSIA)
        private val huaweiApiOrderRussia: HuaweiApi,
        private val objectMapper: ObjectMapper,
        private val log: Logger
) {

    companion object {
        const val ACCOUNT_FLAG_GERMANY_APP_TOUCH = 1
    }

    fun verifyProduct(
            productId: String,
            subscriptionId: String,
            purchaseType: InappType,
            purchaseToken: String,
            accountFlag: Int
    ): ValidationResponse {
        log.info(
                """verifyProduct:
                    productId: $productId, 
                    purchaseType: $purchaseType, 
                    purchaseToken: $purchaseToken, 
                    accountFlag: $accountFlag""".trimIndent()
        )
        return when (purchaseType) {
            InappType.SUBS -> {
                val result = verifySubscription(
                        productId,
                        subscriptionId,
                        purchaseToken,
                        accountFlag
                )
                ValidationResponse.HuaweiSubscriptionResponse(ValidationStatus.VALID, result)
            }
            InappType.INAPP, InappType.CONSUMABLE -> TODO()
        }
    }

    fun verifySubscription(
            productId: String,
            subscriptionId: String,
            purchaseToken: String,
            accountFlag: Int
    ): InAppPurchaseData {
        val api = if (accountFlag == ACCOUNT_FLAG_GERMANY_APP_TOUCH) {
            huaweiApiSubsGermany
        } else {
            huaweiApiSubsRussia
        }

        return try {
            val result = api
                    .verifySubscription(subscriptionId, purchaseToken)
                    .execute()
            if (result.isSuccessful) {
                val huaweiProductVerifyResponse = result.body()
                        ?: throw VerifyProductException(
                                "Cannot verify subscription with error message: ${result.errorBody()}",
                                NullPointerException("Body is null!")
                        )
                if (huaweiProductVerifyResponse.responseCode == 0) {
                    objectMapper.readValue(huaweiProductVerifyResponse.inappPurchaseData!!, InAppPurchaseData::class.java)
                } else {
                    throw VerifyProductException(
                            "Cannot verify subscription with responseCode: ${huaweiProductVerifyResponse.responseCode} message: ${huaweiProductVerifyResponse.responseMessage}",
                            IllegalStateException()
                    )
                }
            } else {
                val errorResponse = objectMapper.readValue(
                        result.errorBody()!!.string(),
                        HuaweiProductVerifyResponse::class.java
                )
                throw VerifyProductException(
                        "Cannot verify subscription with code: ${errorResponse.responseCode} and error message: ${errorResponse.responseMessage}",
                        IllegalStateException()
                )
            }
        } catch (e: Throwable) {
            throw VerifyProductException("Cannot verify subscription with error message: ${e.message}.", e)
        }
    }

    fun cancelSubscription(
            productId: String,
            purchaseToken: String,
            accountFlag: Int
    ): HuaweiSubscriptionCancelResponse {
        val api = if (accountFlag == ACCOUNT_FLAG_GERMANY_APP_TOUCH) {
            huaweiApiSubsGermany
        } else {
            huaweiApiSubsRussia
        }
        return try {
            api.cancelSubscription(productId, purchaseToken).execute().body()!!
        } catch (e: Throwable) {
            e.printStackTrace()
            throw VerifyProductException("Cannot verify subscription with error message: ${e.message}.", e)
        }
    }
}

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class VerifyProductException(
        override val message: String?,
        override val cause: Throwable?
) : RuntimeException(message, cause)
