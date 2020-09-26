package ru.kuchanov.scpreaderapi.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
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

    @GET("sub/applications/{apiVersion}/purchases/get")
    fun verifySubscription(
            @Query("subscriptionId") subscriptionId: String,
            @Query("purchaseToken") purchaseToken: String
    ): Call<HuaweiProductVerifyResponse>
}