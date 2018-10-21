package ru.kuchanov.scpreaderapi.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

    //okhttp + retrofit
    @Bean
    fun loggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor { println(it) }
            .setLevel(HttpLoggingInterceptor.Level.BODY)

    @Bean
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
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
            .setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));

    @Bean
    fun converterFactory(): Converter.Factory = JacksonConverterFactory.create(objectMapper())
}