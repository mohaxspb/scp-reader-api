package ru.kuchanov.scpreaderapi.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path
import ru.kuchanov.scpreaderapi.model.huawei.push.HuaweiPushMessage
import ru.kuchanov.scpreaderapi.model.huawei.push.HuaweiPushMessageSentResponse

interface HuaweiPushApi {

    companion object {
        const val BASE_API_URL = "https://push-api.cloud.huawei.com/v1/"
    }

    @FormUrlEncoded
    @POST("{applicationId}/messages:send")
    fun send(
            @Path("applicationId") applicationId: String,
            @Body huaweiPushMessage: HuaweiPushMessage
    ): Call<HuaweiPushMessageSentResponse>
}