package ru.kuchanov.scpreaderapi.configuration.security

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
                .addMapping("/**")
                .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH", "OPTIONS")
                .allowedOrigins(
                        "http://localhost:4200",
                        "http://localhost:80",
                        "https://scpfoundation.app",
                        "http://scpfoundation.app"
                )
                .allowedHeaders("*")
                .allowCredentials(true)
    }
}
