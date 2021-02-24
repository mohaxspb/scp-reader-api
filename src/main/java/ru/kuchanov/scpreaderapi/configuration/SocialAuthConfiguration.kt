package ru.kuchanov.scpreaderapi.configuration

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson.JacksonFactory
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import ru.kuchanov.scpreaderapi.network.FacebookApi

@Configuration
class SocialAuthConfiguration @Autowired constructor(
        val okHttpClient: OkHttpClient,
        val converterFactory: Converter.Factory,
        val callAdapterFactory: CallAdapter.Factory
) {

    //google auth
    @Value("\${my.api.google.client_id}")
    private lateinit var googleClientId: String

    @Bean
    fun googleIdTokenVerifier(): GoogleIdTokenVerifier = GoogleIdTokenVerifier
            .Builder(NetHttpTransport(), JacksonFactory())
            .setAudience(listOf(googleClientId))
            .build()

    //facebook auth
    @Bean
    fun facebookApi(): FacebookApi {
        val retrofit = Retrofit.Builder()
                .baseUrl(FacebookApi.BASE_API_URL)
                .client(okHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
        return retrofit.create(FacebookApi::class.java)
    }
}
