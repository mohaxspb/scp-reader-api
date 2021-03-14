package ru.kuchanov.scpreaderapi.configuration.monetization

import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import ru.kuchanov.scpreaderapi.configuration.HuaweiApiConfiguration
import ru.kuchanov.scpreaderapi.network.HuaweiPurchaseApi

@Configuration
class HuaweiPurchaseConfiguration @Autowired constructor(
        @Qualifier(HuaweiApiConfiguration.QUALIFIER_OK_HTTP_CLIENT_HUAWEI_PURCHASE_AUTH)
        val huaweiAuthOkHttpClient: OkHttpClient,
        val converterFactory: Converter.Factory,
        val callAdapterFactory: CallAdapter.Factory
) {

    companion object {
        const val QUALIFIER_SUBS_GERMANY_APP_TOUCH = "QUALIFIER_SUBS_GERMANY_APP_TOUCH"
        const val QUALIFIER_ORDER_GERMANY_APP_TOUCH = "QUALIFIER_ORDER_GERMANY_APP_TOUCH"
        const val QUALIFIER_SUBS_RUSSIA = "QUALIFIER_SUBS_RUSSIA"
        const val QUALIFIER_ORDER_RUSSIA = "QUALIFIER_ORDER_RUSSIA"
    }

    @Bean
    @Qualifier(QUALIFIER_SUBS_GERMANY_APP_TOUCH)
    fun huaweiApiSubsGermanyAppTouch(): HuaweiPurchaseApi {
        val retrofit = Retrofit.Builder()
                .baseUrl(HuaweiPurchaseApi.SUBS_GERMANY_APP_TOUCH_API_URL)
                .client(huaweiAuthOkHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
        return retrofit.create(HuaweiPurchaseApi::class.java)
    }

    @Bean
    @Qualifier(QUALIFIER_ORDER_GERMANY_APP_TOUCH)
    fun huaweiApiOrderGermanyAppTouch(): HuaweiPurchaseApi {
        val retrofit = Retrofit.Builder()
                .baseUrl(HuaweiPurchaseApi.ORDER_GERMANY_APP_TOUCH_API_URL)
                .client(huaweiAuthOkHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
        return retrofit.create(HuaweiPurchaseApi::class.java)
    }

    @Bean
    @Qualifier(QUALIFIER_SUBS_RUSSIA)
    fun huaweiApiSubsRussia(): HuaweiPurchaseApi {
        val retrofit = Retrofit.Builder()
                .baseUrl(HuaweiPurchaseApi.SUBS_RUSSIA_API_URL)
                .client(huaweiAuthOkHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
        return retrofit.create(HuaweiPurchaseApi::class.java)
    }

    @Bean
    @Qualifier(QUALIFIER_ORDER_RUSSIA)
    fun huaweiApiOrderRussia(): HuaweiPurchaseApi {
        val retrofit = Retrofit.Builder()
                .baseUrl(HuaweiPurchaseApi.ORDER_RUSSIA_API_URL)
                .client(huaweiAuthOkHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
        return retrofit.create(HuaweiPurchaseApi::class.java)
    }
}