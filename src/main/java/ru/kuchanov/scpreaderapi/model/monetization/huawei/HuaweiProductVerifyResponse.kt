package ru.kuchanov.scpreaderapi.model.monetization.huawei

data class HuaweiProductVerifyResponse(
        /**
         * Result code. The options are as follows:
         * ● 0: success
         * ● Other values: failure.
         * For details about the result codes,
         * please refer to API [Result Codes](https://developer.huawei.com/consumer/en/doc/development/HMS-References/iap-api-specification-related-v4#API-ErrorCode).
         */
        val responseCode: Int,
        val responseMessage: String,
        /**
         * JSON string that contains purchase details.
         * For details about the format, please refer to the description of [InappPurchaseDetail](https://developer.huawei.com/consumer/en/doc/development/HMS-References/iap-api-subscription-service-purchase-token-verification-v4#InappPurchaseDetail).
         */
        val inappPurchaseData: String,
)