package ru.kuchanov.scpreaderapi.configuration

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson.JacksonFactory
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SocialAuthConfiguration {

    @Autowired
    private lateinit var log: Logger

    @Value("\${my.api.google.client_id}")
    private lateinit var googleClientId: String

    @Bean
    fun provideGoogleApi(): GoogleIdTokenVerifier  = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory())
            .setAudience(listOf(googleClientId))
            .build()
}