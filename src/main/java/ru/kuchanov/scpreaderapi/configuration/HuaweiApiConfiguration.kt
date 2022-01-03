package ru.kuchanov.scpreaderapi.configuration

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
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
import ru.kuchanov.scpreaderapi.Application.Companion.HUAWEI_LOGGER
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.auth.huawei.HuaweiOAuthAccessToken
import ru.kuchanov.scpreaderapi.model.huawei.auth.TokenResponse
import ru.kuchanov.scpreaderapi.network.HuaweiAccountApi
import ru.kuchanov.scpreaderapi.network.HuaweiAuthApi
import ru.kuchanov.scpreaderapi.network.HuaweiPushApi
import ru.kuchanov.scpreaderapi.repository.auth.huawei.HuaweiAccessTokenRepository
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets

@Configuration
class HuaweiApiConfiguration @Autowired constructor(
        @Qualifier(NetworkConfiguration.QUALIFIER_OK_HTTP_CLIENT_NOT_LOGGING)
        private val okHttpClient: OkHttpClient,
        private val converterFactory: Converter.Factory,
        private val callAdapterFactory: CallAdapter.Factory,
        private val huaweiAccessTokenRepository: HuaweiAccessTokenRepository,
        @Value("\${my.api.huawei.client_id}") private val huaweiClientId: String,
        @Value("\${my.api.huawei.client_secret}") private val huaweiClientSecret: String,
        @Qualifier(HUAWEI_LOGGER) private val log: Logger
) {

    companion object {
        const val QUALIFIER_OK_HTTP_CLIENT_HUAWEI_PURCHASE_AUTH = "QUALIFIER_OK_HTTP_CLIENT_HUAWEI_PURCHASE_AUTH"
        const val QUALIFIER_OK_HTTP_CLIENT_HUAWEI_COMMON_AUTH = "QUALIFIER_OK_HTTP_CLIENT_HUAWEI_COMMON_AUTH"

        const val HUAWEI_PUSH_SENDING_SUCCESS_CODE = "80000000"

        /**
         * see msg in response for failed tokens:
         * "msg": "{\"success\":3,\"failure\":1,\"illegal_tokens\":[\"xxx\"]}"
         */
        const val HUAWEI_PUSH_SENDING_PARTIAL_SUCCESS_CODE = "80100000"

        const val HUAWEI_COMMON_API_AUTH_ERROR_CODE = "80200001"
        const val HUAWEI_COMMON_API_AUTH_EXPIRED_ERROR_CODE = "80200003"
    }

    @Bean
    fun huaweiAuthApi(): HuaweiAuthApi {
        val retrofit = Retrofit.Builder()
                .baseUrl(HuaweiAuthApi.BASE_API_URL)
                .client(createLoggingOkHttpClient())
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
        return retrofit.create(HuaweiAuthApi::class.java)
    }

    @Bean
    fun huaweiAccountApi(): HuaweiAccountApi {
        val retrofit = Retrofit.Builder()
                .baseUrl(HuaweiAccountApi.BASE_API_URL)
                .client(createLoggingOkHttpClient())
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
        return retrofit.create(HuaweiAccountApi::class.java)
    }

    @Bean
    @Qualifier(QUALIFIER_OK_HTTP_CLIENT_HUAWEI_PURCHASE_AUTH)
    fun createOkHttpClientHuaweiPurchase(): OkHttpClient =
            createAuthorizedOkHttpClient(
                    ::createHuaweiPurchaseApiAuthValue,
                    ::huaweiPurchaseRequestUnauthorizedResolver
            )

    @Bean
    @Qualifier(QUALIFIER_OK_HTTP_CLIENT_HUAWEI_COMMON_AUTH)
    fun createOkHttpClientHuaweiCommon(): OkHttpClient =
            createAuthorizedOkHttpClient(
                    ::createHuaweiCommonApiAuthValue,
                    ::huaweiCommonRequestUnauthorizedResolver
            )

    @Bean
    fun huaweiPushApi(): HuaweiPushApi {
        val retrofit = Retrofit.Builder()
                .baseUrl(HuaweiPushApi.BASE_API_URL)
                .client(createOkHttpClientHuaweiCommon())
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
        return retrofit.create(HuaweiPushApi::class.java)
    }

    private fun createLoggingOkHttpClient() =
            okHttpClient.newBuilder()
                    .addInterceptor(
                            HttpLoggingInterceptor { log.debug("OkHttp: $it") }
                                    .setLevel(HttpLoggingInterceptor.Level.BODY)
                    )
                    .build()

    private fun createAuthorizedOkHttpClient(
            authValueCreator: (HuaweiOAuthAccessToken) -> String,
            unauthorizedRequestResolver: (okhttp3.Response) -> Boolean
    ): OkHttpClient {
        val unAuthAccessTokenInterceptor = Interceptor { chain ->
            val initialRequest = chain.request()
            val initialResponse: okhttp3.Response = chain.proceed(initialRequest)
//            log.error("initialResponse: ${initialResponse.isSuccessful}")
//            log.error("initialResponse: ${initialResponse.code()}")
//            log.error("initialResponse: ${initialResponse.request().url()}")
            val initialResponseBody = initialResponse.body
                    ?: throw NullPointerException("Body is null while request: ${initialRequest.url.toUrl()}!")
//            log.error("initialResponseBody: $initialResponseBody")
            val initialResponseBodyContentType = initialResponseBody.contentType()
//            log.error("initialResponseBodyContentType: $initialResponseBodyContentType")
            val initialResponseBodyContent = initialResponseBody.string()
//            log.error("initialResponseBodyContent: $initialResponseBodyContent")
            val bodyToCheck = ResponseBody.create(initialResponseBodyContentType, initialResponseBodyContent)
//            log.error("bodyToCheck: $bodyToCheck")
            val responseToCheck = initialResponse.newBuilder().body(bodyToCheck).build()
            val unauthResult = unauthorizedRequestResolver(responseToCheck)
            responseToCheck.close()
//            log.error("unauthResult: $unauthResult")
            if (unauthResult) {
//                log.error("unauthorizedRequestResolver: true")
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
                                authValueCreator(token)
                        )
                        .build()
                chain.proceed(authorizedRequest)
            } else {
                initialResponse.newBuilder().body(bodyToCheck).build()
            }
        }

        val accessTokenInterceptor = Interceptor { chain ->
            val token = huaweiAccessTokenRepository
                    .findFirstByClientId(huaweiClientId)
                    ?.let { authValueCreator(it) }
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

        return createLoggingOkHttpClient()
                .newBuilder()
                .addInterceptor(accessTokenInterceptor)
                .addInterceptor(unAuthAccessTokenInterceptor)
                .build()
    }

    private fun createHuaweiPurchaseApiAuthValue(token: HuaweiOAuthAccessToken): String {
        val tokenValue = "APPAT:${token.accessToken}"
        return "${ScpReaderConstants.Api.HEADER_PART_BASIC} " + Base64Utils.encodeToString(tokenValue.toByteArray(StandardCharsets.UTF_8))
    }

    private fun huaweiPurchaseRequestUnauthorizedResolver(initialResponse: okhttp3.Response) =
            initialResponse.code == HttpURLConnection.HTTP_UNAUTHORIZED

    private fun createHuaweiCommonApiAuthValue(token: HuaweiOAuthAccessToken): String =
            "${ScpReaderConstants.Api.HEADER_PART_BEARER} ${token.accessToken}"

    private fun huaweiCommonRequestUnauthorizedResolver(initialResponse: okhttp3.Response): Boolean {
        // body can be read only once, so create copy of it to prevent initialResponse corrupting.
        val responseBodyAsString = initialResponse.body!!.string()
//        log.error("responseBodyAsString: $responseBodyAsString")
        return responseBodyAsString.contains(""""code":"$HUAWEI_COMMON_API_AUTH_ERROR_CODE"""")
                || responseBodyAsString.contains(""""code":"$HUAWEI_COMMON_API_AUTH_EXPIRED_ERROR_CODE"""")
    }
}
