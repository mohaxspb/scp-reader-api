package ru.kuchanov.scpreaderapi.configuration

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson.JacksonFactory
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.ServiceActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import ru.kuchanov.scpreaderapi.network.FacebookApi

@Suppress("unused")
@Configuration
class SocialAuthConfiguration {

    @Autowired
    private lateinit var log: Logger

    //google auth
    @Value("\${my.api.ru.google.client_id}")
    private lateinit var googleClientIdRu: String
    @Value("\${my.api.en.google.client_id}")
    private lateinit var googleClientIdEn: String
    @Value("\${my.api.pl.google.client_id}")
    private lateinit var googleClientIdPl: String
    @Value("\${my.api.de.google.client_id}")
    private lateinit var googleClientIdDe: String
    @Value("\${my.api.fr.google.client_id}")
    private lateinit var googleClientIdFr: String
    @Value("\${my.api.es.google.client_id}")
    private lateinit var googleClientIdEs: String
    @Value("\${my.api.it.google.client_id}")
    private lateinit var googleClientIdIt: String
    @Value("\${my.api.pt.google.client_id}")
    private lateinit var googleClientIdPt: String
    @Value("\${my.api.ch.google.client_id}")
    private lateinit var googleClientIdCh: String

    @Bean
    fun googleIdTokenVerifier(): GoogleIdTokenVerifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory())
            .setAudience(listOf(
                    googleClientIdRu,
                    googleClientIdEn,
                    googleClientIdPl,
                    googleClientIdDe,
                    googleClientIdFr,
                    googleClientIdEs,
                    googleClientIdIt,
                    googleClientIdPt,
                    googleClientIdCh
            ))
            .build()

    //facebook
    @Autowired
    private lateinit var okHttpClient: OkHttpClient
    @Autowired
    private lateinit var converterFactory: Converter.Factory
    @Autowired
    private lateinit var callAdapterFactory: CallAdapter.Factory

    @Bean
    fun retrofit(): Retrofit = Retrofit.Builder()
            .baseUrl(FacebookApi.BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()

    @Bean
    fun facebookApi(): FacebookApi = retrofit().create(FacebookApi::class.java)

    //vk
    @Value("\${my.api.vk.app_id}")
    var vkAppId: Int? = null

    @Value("\${my.api.vk.client_secret}")
    lateinit var vkClientSecret: String

    @Value("\${my.api.vk.service_access_key}")
    lateinit var vkServiceAccessKey: String

    @Bean
    fun transportClient(): HttpTransportClient = HttpTransportClient()

    @Bean
    fun vkApiClient(): VkApiClient = VkApiClient(transportClient())

    @Bean
    fun serviceActor(): ServiceActor = ServiceActor(vkAppId, vkClientSecret, vkServiceAccessKey)
}