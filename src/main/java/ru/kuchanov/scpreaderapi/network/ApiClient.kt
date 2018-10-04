package ru.kuchanov.scpreaderapi.network

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.ServiceActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.photos.responses.GetResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import ru.kuchanov.scpreaderapi.model.facebook.FacebookApi
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Service
class ApiClient {

    @Bean
    fun loggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor { println(it) }
            .setLevel(HttpLoggingInterceptor.Level.BODY)

    @Bean
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    @Bean
    fun callAdapterFactory(): RxJavaCallAdapterFactory = RxJavaCallAdapterFactory.create()

    @Bean
    fun retrofit() = Retrofit.Builder()
            .baseUrl(FacebookApi.BASE_API_URL)
            .client(okHttpClient())
            .addCallAdapterFactory(callAdapterFactory())
            .build()

    //vk
    @Value("\${my.api.vk.app_id}")
    var vkAppId: Int? = null

    @Value("\${my.api.vk.client_secret}")
    lateinit var vkClientSecret: String

    @Value("\${my.api.vk.service_access_key}")
    lateinit var vkServiceAccessKey: String

    @Value("\${my.api.vk.art_scp.group_id}")
    var artScpVkGroupId: Int? = null

    @Value("\${my.api.vk.art_scp.album_id}")
    var artScpVkAlbumId: Int? = null

    lateinit var transportClient: HttpTransportClient
    lateinit var vk: VkApiClient
    lateinit var actor: ServiceActor

    @PostConstruct
    fun initClassMembers() {
        transportClient = HttpTransportClient()
        vk = VkApiClient(transportClient)
        actor = ServiceActor(vkAppId, vkClientSecret, vkServiceAccessKey)
    }

    fun getVkAccessToken() = vk.oauth()
            .serviceClientCredentialsFlow(vkAppId, vkClientSecret)
            .execute().accessToken

    fun getScpArtPhotosFromVk(): GetResponse? = vk.photos().get(actor)
            .ownerId(artScpVkGroupId)
            .albumId(artScpVkAlbumId.toString())
            .photoSizes(true)
            .execute()
}