package ru.kuchanov.scpreaderapi

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.context.annotation.Bean
import ru.kuchanov.scpreaderapi.network.ApiClient


@SpringBootApplication
@EnableScheduling
class Application : SpringBootServletInitializer() {
    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(Application::class.java)
    }

    @Bean
    fun logger() = LoggerFactory.getLogger("application")

    @Bean
    fun apiClient() = ApiClient()

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
