package ru.kuchanov.scpreaderapi.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Converter
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import ru.kuchanov.scpreaderapi.network.ApiClient
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

@Configuration
class NetworkConfiguration {

    //okHttp + retrofit
    @Bean
    fun loggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor { println("OkHttp: $it") }
            .setLevel(HttpLoggingInterceptor.Level.BODY)

    @Bean
    @Qualifier(QUALIFIER_OK_HTTP_CLIENT_COMMON)
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .connectTimeout(ApiClient.OK_HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiClient.OK_HTTP_READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiClient.OK_HTTP_WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()

    @Bean
    @Qualifier(QUALIFIER_OK_HTTP_CLIENT_NOT_LOGGING)
    fun notLoggingOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(ApiClient.OK_HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiClient.OK_HTTP_READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiClient.OK_HTTP_WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()

    @Bean
    fun callAdapterFactory(): RxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))

    @Bean
    fun converterFactory(): Converter.Factory = JacksonConverterFactory.create(objectMapper())
    //okHttp + retrofit END

    companion object {
        const val QUALIFIER_OK_HTTP_CLIENT_COMMON = "okHttpCommon"
        const val QUALIFIER_OK_HTTP_CLIENT_NOT_LOGGING = "okHttpNotLogging"
    }
}
