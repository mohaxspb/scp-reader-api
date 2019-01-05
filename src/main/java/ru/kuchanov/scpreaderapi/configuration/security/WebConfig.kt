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
                        "http://localhost:443",
                        "https://scpfoundation.app:443",
                        "https://scpfoundation.app",
                        "http://scpfoundation.app:80",
                        "http://scpfoundation.app",
                        "http://37.143.14.68:80",
                        "http://37.143.14.68"
                )
                .allowedHeaders("*")
                .allowCredentials(true)
    }
}