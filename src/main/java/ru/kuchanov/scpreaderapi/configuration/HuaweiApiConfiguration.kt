package ru.kuchanov.scpreaderapi.configuration

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.Base64Utils
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.auth.huawei.HuaweiOAuthAccessToken
import ru.kuchanov.scpreaderapi.model.huawei.auth.TokenResponse
import ru.kuchanov.scpreaderapi.network.HuaweiAuthApi
import ru.kuchanov.scpreaderapi.repository.auth.huawei.HuaweiAccessTokenRepository
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets

@Configuration
class HuaweiApiConfiguration @Autowired constructor(
        @Qualifier(NetworkConfiguration.QUALIFIER_OK_HTTP_CLIENT_COMMON)
        private val okHttpClient: OkHttpClient,
        private val loggingInterceptor: HttpLoggingInterceptor,
        private val converterFactory: Converter.Factory,
        private val callAdapterFactory: CallAdapter.Factory,
        private val huaweiAccessTokenRepository: HuaweiAccessTokenRepository,
        @Value("\${my.api.huawei.client_id}") private val huaweiClientId: String,
        @Value("\${my.api.huawei.client_secret}")private  val huaweiClientSecret: String,
        private val log: Logger
) {

    companion object {
        const val QUALIFIER_OK_HTTP_CLIENT_HUAWEI_AUTH = "QUALIFIER_OK_HTTP_CLIENT_HUAWEI_AUTH"
    }

    @Bean
    fun huaweiAuthApi(): HuaweiAuthApi {
        val retrofit = Retrofit.Builder()
                .baseUrl(HuaweiAuthApi.BASE_API_URL)
                .client(okHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
        return retrofit.create(HuaweiAuthApi::class.java)
    }

    @Bean
    @Qualifier(QUALIFIER_OK_HTTP_CLIENT_HUAWEI_AUTH)
    fun createAuthorizedOkHttpClient(): OkHttpClient {
        val unAuthAccessTokenInterceptor = Interceptor { chain ->
            val initialRequest = chain.request()
            val initialResponse = chain.proceed(initialRequest)
            if (initialResponse.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                val response: Response<TokenResponse> = huaweiAuthApi()
                        .getAccessToken(
                                ScpReaderConstants.Api.GRANT_TYPE_CLIENT_CREDENTIALS,
                                huaweiClientId,
                                huaweiClientSecret
                        )
                        .execute()
                val tokenResponseBody = if (response.isSuccessful) {
                    response.body()
                            ?: throw NullPointerException("Body is null while get accessToken by refreshToken!")
                } else {
                    log.error("Error while get accessToken: ${response.code()}")
                    log.error(
                            "Error while get accessToken: ${response.errorBody()?.string()}"
                    )
                    throw IllegalStateException("Error while get accessToken for Huawei")
                }

                val token = HuaweiOAuthAccessToken(
                        clientId = huaweiClientId,
                        accessToken = tokenResponseBody.accessToken,
                        expiresIn = tokenResponseBody.expiresIn,
                        tokenType = tokenResponseBody.tokenType
                )
                huaweiAccessTokenRepository.save(token)

                //we need to close response to be able to start new request
                initialResponse.close()
                val authorizedRequest = initialRequest
                        .newBuilder()
                        .header(
                                ScpReaderConstants.Api.HEADER_AUTHORIZATION,
                                createAuthValue(token)
                        )
                        .build()
                chain.proceed(authorizedRequest)
            } else {
                initialResponse
            }
        }

        val accessTokenInterceptor = Interceptor { chain ->
            val token = huaweiAccessTokenRepository.findFirstByClientId(huaweiClientId)?.let { createAuthValue(it) }
                    ?: ""
            log.error("accessTokenInterceptor: $token")
            val request =
                    chain
                            .request()
                            .newBuilder()
                            .header(
                                    ScpReaderConstants.Api.HEADER_AUTHORIZATION,
                                    token
                            )
                            .build()
            chain.proceed(request)
        }

        return okHttpClient.newBuilder()
                .addInterceptor(accessTokenInterceptor)
                .addInterceptor(unAuthAccessTokenInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()
    }

    private fun createAuthValue(token: HuaweiOAuthAccessToken): String {
        val tokenValue = "APPAT:${token.accessToken}"
        return "${ScpReaderConstants.Api.HEADER_PART_BASIC} " + Base64Utils.encodeToString(tokenValue.toByteArray(StandardCharsets.UTF_8))
    }
}
