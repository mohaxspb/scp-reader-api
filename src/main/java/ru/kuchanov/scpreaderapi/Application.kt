package ru.kuchanov.scpreaderapi

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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


@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableAsync
class Application : SpringBootServletInitializer() {

    @Primary
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

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder =
            application.sources(Application::class.java)

    companion object {
        const val APPLICATION_LOGGER = "application"
        const val CACHE_LOGGER = "cache"
        const val HUAWEI_LOGGER = "huawei"

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}
