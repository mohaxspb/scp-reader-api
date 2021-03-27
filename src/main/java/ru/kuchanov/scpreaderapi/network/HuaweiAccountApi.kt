package ru.kuchanov.scpreaderapi.network

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import ru.kuchanov.scpreaderapi.model.huawei.account.HuaweiAccountResponse

/**
 * https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/get-user-info-0000001060261938#EN-US_TOPIC_0000001060261938__section18490162375211
 */
interface HuaweiAccountApi {

    companion object {
        const val BASE_API_URL = "https://account.cloud.huawei.com/"
    }

    @FormUrlEncoded
    @POST("rest.php?nsp_svc=GOpen.User.getInfo")
    fun getAccount(
            @Field("access_token") accessToken: String,
            /**
             * Indicates whether the nickname is returned preferentially.
             * 0: no (default value)
             * 1: yes
             */
            @Field("getNickName") getNickName: Int = 1
    ): Call<HuaweiAccountResponse>
}