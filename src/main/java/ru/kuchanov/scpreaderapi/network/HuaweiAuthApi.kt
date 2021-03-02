package ru.kuchanov.scpreaderapi.network

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import ru.kuchanov.scpreaderapi.model.huawei.auth.TokenResponse

/**
 * For url see this doc:
 * https://developer.huawei.com/consumer/en/doc/38054564#h2-1580973380498
 */
interface HuaweiAuthApi {

    companion object {
        const val BASE_API_URL = "https://oauth-login.cloud.huawei.com/"
    }

    @FormUrlEncoded
    @POST("oauth2/v2/token")
    fun getAccessToken(
            @Field("grant_type") grantType: String,
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String
    ): Call<TokenResponse>
}