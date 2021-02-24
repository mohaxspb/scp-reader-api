package ru.kuchanov.scpreaderapi.configuration.monetization

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisherScopes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.kuchanov.scpreaderapi.utils.FileUtils

@Configuration
class GooglePurchaseConfiguration {

    companion object {
        const val ANDROID_DEVELOPER_SERVICE_KEY = "googlePlay/googlePlayAndroidDeveloperServiceKey.json"
        const val APPLICATION_NAME = "web-app"
    }

    @Bean
    fun httpTransport(): HttpTransport = GoogleNetHttpTransport.newTrustedTransport()

    @Bean
    fun jsonFactory(): JacksonFactory = JacksonFactory.getDefaultInstance()

    @Bean
    fun googleCredential(): GoogleCredential = GoogleCredential
            .fromStream(FileUtils.getFileAsInputStreamFromResources(ANDROID_DEVELOPER_SERVICE_KEY))
            .createScoped(setOf(AndroidPublisherScopes.ANDROIDPUBLISHER))

    @Bean
    fun androidPublisher(): AndroidPublisher = AndroidPublisher.Builder(
            httpTransport(),
            jsonFactory(),
            googleCredential()
    ).setApplicationName(APPLICATION_NAME).build()
}