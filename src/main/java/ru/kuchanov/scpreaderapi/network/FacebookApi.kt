package ru.kuchanov.scpreaderapi.network

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import ru.kuchanov.scpreaderapi.model.facebook.DebugTokenResponse
import ru.kuchanov.scpreaderapi.model.facebook.FacebookProfileResponse

interface FacebookApi {

    companion object {
        const val BASE_API_URL = "https://graph.facebook.com/v3.1/"
    }

    /**
     * @param inputToken token to debug
     * @param accessToken in form "APP_ID|APP_SECRET"
     */
    @GET("debug_token")
    fun debugToken(
            @Query("input_token") inputToken: String,
            @Query("access_token") accessToken: String
    ): Single<DebugTokenResponse>

    @GET("me?fields=email,name,first_name,middle_name,last_name,picture.width(500).height(500){url,height,width}")
    fun profile(@Query("access_token") accessToken: String): Single<FacebookProfileResponse>
}