package ru.kuchanov.scpreaderapi

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import ru.kuchanov.scpreaderapi.network.ModelConverter


@SpringBootApplication
@EnableScheduling
@EnableAsync
class Application : SpringBootServletInitializer() {

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder =
            application.sources(Application::class.java)

    @Bean
    fun logger(): Logger =
            LoggerFactory.getLogger("application")

    @Bean
    fun modelConverter() =
            ModelConverter()
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
