package ru.kuchanov.scpreaderapi.configuration.firebase

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
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
        private val log: Logger,
        private val objectMapper: ObjectMapper
) {

    @Bean
    fun provideFirebase(): FirebaseApp? {
        //init defualt app
        val defaultAccountKeyFileName = "firebase/keys/scp-database-reader-firebase-adminsdk.json"
        val defaultFirebaseInstanceName = "scp-database-reader"
        initFirebase(defaultAccountKeyFileName, defaultFirebaseInstanceName, true)

        ScpReaderConstants.Firebase.FirebaseInstance.values().forEach { firebaseInstance ->
            val accountKeyFileName = "firebase/keys/serviceAccountKey${firebaseInstance.lang.capitalize()}.json"
            val firebaseInstanceName = firebaseInstance.lang
            initFirebase(accountKeyFileName, firebaseInstanceName, false)
        }

        return FirebaseApp.getInstance()
    }

    private fun initFirebase(accountKeyFileName: String, firebaseInstanceName: String, isDefaultApp: Boolean) {
        val accountKeyFileAsStream = ClassPathResource(accountKeyFileName).inputStream
        val accountKeyFileAsString = StreamUtils.copyToString(
                accountKeyFileAsStream,
                Charset.forName("UTF-8")
        )
        val accountKey = objectMapper.readValue(accountKeyFileAsString, FirebaseAccountKey::class.java)

        log.debug("accountKey: $accountKey")

        val databaseUrl = "https://${accountKey.projectId}.firebaseio.com"
        //log.debug("databaseUrl: $databaseUrl")
        //println("databaseUrl: $databaseUrl")
        val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(ClassPathResource(accountKeyFileName).inputStream))
                .setDatabaseUrl(databaseUrl)
                .build()

        if (isDefaultApp) {
            FirebaseApp.initializeApp(options)
        }
        FirebaseApp.initializeApp(options, firebaseInstanceName)
    }
}
