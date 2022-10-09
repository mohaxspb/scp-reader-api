package ru.kuchanov.scpreaderapi

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
//import org.springframework.security.oauth2.provider.token.TokenStore
//import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore
import javax.sql.DataSource


@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableAsync
class Application : SpringBootServletInitializer() {

    @Primary
    @Qualifier(APPLICATION_LOGGER)
    @Bean
    fun logger(): Logger =
            LoggerFactory.getLogger(APPLICATION_LOGGER)

    @Qualifier(CACHE_LOGGER)
    @Bean
    fun cacheLogger(): Logger =
            LoggerFactory.getLogger(CACHE_LOGGER)

    @Qualifier(HUAWEI_LOGGER)
    @Bean
    fun huaweiLogger(): Logger =
            LoggerFactory.getLogger(HUAWEI_LOGGER)

    @Qualifier(GOOGLE_LOGGER)
    @Bean
    fun googleLogger(): Logger =
        LoggerFactory.getLogger(GOOGLE_LOGGER)

    @Qualifier(PARSING_LOGGER)
    @Bean
    fun parsingLogger(): Logger =
            LoggerFactory.getLogger(PARSING_LOGGER)

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    //do not move to constructor - there are circular dependency error
//    @Autowired
//    private lateinit var dataSource: DataSource

//    @Bean
//    fun tokenStore(): TokenStore =
//        JdbcTokenStore(dataSource)

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder =
            application.sources(Application::class.java)

    companion object {
        const val APPLICATION_LOGGER = "application"
        const val CACHE_LOGGER = "cache"
        const val HUAWEI_LOGGER = "huawei"
        const val GOOGLE_LOGGER = "google"
        const val PARSING_LOGGER = "parsing"

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}
