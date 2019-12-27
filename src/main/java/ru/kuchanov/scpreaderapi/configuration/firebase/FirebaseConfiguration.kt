package ru.kuchanov.scpreaderapi.configuration.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.codehaus.jackson.map.ObjectMapper
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.util.StreamUtils
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.model.firebase.FirebaseAccountKey
import java.nio.charset.Charset

@Configuration
class FirebaseConfiguration @Autowired constructor(
        val log: Logger
) {

    @Bean
    fun provideFirebase(): FirebaseApp? {
        ScpReaderConstants.Firebase.FirebaseInstance.values().forEachIndexed { index, firebaseInstance ->
            val accountKeyFileName = "firebase/keys/serviceAccountKey${firebaseInstance.lang.capitalize()}.json"
            val accountKeyFileAsStream = ClassPathResource(accountKeyFileName).inputStream
            val accountKeyFileAsString = StreamUtils.copyToString(
                    accountKeyFileAsStream,
                    Charset.forName("UTF-8")
            )
            val accountKey = ObjectMapper().readValue(accountKeyFileAsString, FirebaseAccountKey::class.java)

            log.debug("accountKey: $accountKey")

            val databaseUrl = "https://${accountKey.projectId}.firebaseio.com"
            //log.debug("databaseUrl: $databaseUrl")
            //println("databaseUrl: $databaseUrl")
            val options = FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(ClassPathResource(accountKeyFileName).inputStream))
                    .setDatabaseUrl(databaseUrl)
                    .build()

            if (index == 0) {
                FirebaseApp.initializeApp(options)
            }
            FirebaseApp.initializeApp(options, firebaseInstance.lang)
        }

        return FirebaseApp.getInstance()
    }
}
