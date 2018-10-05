package ru.kuchanov.scpreaderapi.network

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.ServiceActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.photos.responses.GetResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.codehaus.jackson.map.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.model.dto.auth.CommonUserData
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
    fun retrofit(): Retrofit = Retrofit.Builder()
            .baseUrl(FacebookApi.BASE_API_URL)
            .client(okHttpClient())
            .addCallAdapterFactory(callAdapterFactory())
            .build()

    @Bean
    fun facebookApi(): FacebookApi = retrofit().create(FacebookApi::class.java)

    @Autowired
    private lateinit var googleIdTokenVerifier: GoogleIdTokenVerifier

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

    //facebook
    @Value("\${my.api.facebook.client_id}")
    var facebookClientId: Long? = null

    @Value("\${my.api.facebook.client_secret}")
    lateinit var facebookClientSecret: String

    //todo think if we can init in @Bean funs
    @PostConstruct
    fun initClassMembers() {
        transportClient = HttpTransportClient()
        vk = VkApiClient(transportClient)
        actor = ServiceActor(vkAppId, vkClientSecret, vkServiceAccessKey)
    }

    fun getVkAppAccessToken() = vk.oauth()
            .serviceClientCredentialsFlow(vkAppId, vkClientSecret)
            .execute().accessToken

    fun getScpArtPhotosFromVk(): GetResponse? = vk.photos().get(actor)
            .ownerId(artScpVkGroupId)
            .albumId(artScpVkAlbumId.toString())
            .photoSizes(true)
            .execute()

    fun getUserDataFromProvider(
            provider: ScpReaderConstants.SocialProvider,
            token: String
    ): CommonUserData = when (provider) {
        ScpReaderConstants.SocialProvider.GOOGLE -> {
            val googleIdToken: GoogleIdToken? = googleIdTokenVerifier.verify(token)
            googleIdToken?.let {
                val avatar = googleIdToken.payload["picture"] as? String
                        ?: ScpReaderConstants.DEFAULT_AVATAR_URL
                val fullName = googleIdToken.payload["name"] as? String
                        ?: ScpReaderConstants.DEFAULT_FULL_NAME
                val firstName = googleIdToken.payload["given_name"] as? String
                        ?: ScpReaderConstants.DEFAULT_FULL_NAME
                val secondName = googleIdToken.payload["family_name"] as? String
                        ?: ScpReaderConstants.DEFAULT_FULL_NAME
                CommonUserData(
                        id = googleIdToken.payload.subject,
                        email = googleIdToken.payload.email,
                        firstName = firstName,
                        secondName = secondName,
                        fullName = fullName,
                        avatarUrl = avatar

                )
            } ?: throw IllegalStateException("Failed to verify idToken")
        }
        ScpReaderConstants.SocialProvider.FACEBOOK -> {
            val verifiedToken = facebookApi()
                    .debugToken(token, "$facebookClientId|$facebookClientSecret")
                    .blockingGet()

            if (verifiedToken.data?.appId != facebookClientId) {
                throw IllegalArgumentException("Facebook appId not equals correct one!")
            }

            val facebookProfile = facebookApi().profile(token).blockingGet()
            CommonUserData(
                    id = facebookProfile.id.toString(),
                    email = facebookProfile.email!!,
                    fullName = "${facebookProfile.firstName} ${facebookProfile.lastName}",
                    firstName = facebookProfile.firstName,
                    lastName = facebookProfile.lastName,
                    avatarUrl = facebookProfile.picture?.data?.url
            )
        }
        ScpReaderConstants.SocialProvider.VK -> ObjectMapper().readValue(token, CommonUserData::class.java)
        else -> throw IllegalArgumentException("Unexpected provider: $provider")
    }
}