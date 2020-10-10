package ru.kuchanov.scpreaderapi.network

import retrofit2.Call
import retrofit2.http.*
import ru.kuchanov.scpreaderapi.model.monetization.huawei.HuaweiProductVerifyResponse

/**
 * For url see this doc:
 * https://developer.huawei.com/consumer/en/doc/development/HMS-References/iap-api-specification-related-v4#h1-1578554539083-0
 */
interface HuaweiApi {

    companion object {
        const val SUBS_RUSSIA_API_URL = "https://subscr-drru.iap.hicloud.com"
        const val ORDER_RUSSIA_API_URL = "https://orders-drru.iap.hicloud.com"
        const val SUBS_GERMANY_APP_TOUCH_API_URL = "https://subscr-at-dre.iap.dbankcloud.com"
        const val ORDER_GERNAMY_APP_TOUCH_API_URL = "https://orders-at-dre.iap.dbankcloud.com"
    }

    //todo auth https://developer.huawei.com/consumer/en/doc/development/HMS-References/iap-obtain-application-level-AT-v4
    @FormUrlEncoded
    @POST("sub/applications/v2/purchases/get")
    fun verifySubscription(
            @Field("subscriptionId") subscriptionId: String,
            @Field("purchaseToken") purchaseToken: String
    ): Call<HuaweiProductVerifyResponse>
}