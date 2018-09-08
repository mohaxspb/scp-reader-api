package ru.kuchanov.scpreaderapi.controller

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.Constants
import ru.kuchanov.scpreaderapi.network.ApiClient
import ru.kuchanov.scpreaderapi.network.ModelConverter
import ru.kuchanov.scpreaderapi.service.firebase.FirebaseService


@RestController
@RequestMapping("/${Constants.FIREBASE_PATH}")
class FirebaseController {

    @Autowired
    private lateinit var log: Logger

    @Autowired
    private lateinit var apiClient: ApiClient

    @Autowired
    private lateinit var modelConverter: ModelConverter

    @Autowired
    private lateinit var firebaseService: FirebaseService

    @GetMapping("/{lang}/users/updateFromFirebase")
    fun test(@PathVariable(value = "lang") lang: Constants.Firebase.FirebaseInstance) {
        firebaseService.test(lang)
    }

    @GetMapping("/{lang}/users/all")
    fun getAllUsersForLang(@PathVariable(value = "lang") lang: Constants.Firebase.FirebaseInstance) =
            firebaseService.getAllUsersForLang(lang.lang)

    @GetMapping("/{lang}/users/count")
    fun getUsersCountForLang(@PathVariable(value = "lang") lang: Constants.Firebase.FirebaseInstance) =
            firebaseService.getAllUsersForLang(lang.lang).size
}