package ru.kuchanov.scpreaderapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.service.firebase.FirebaseService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.FIREBASE)
class FirebaseController @Autowired constructor(
        val firebaseService: FirebaseService
) {

    @GetMapping("/updateFromFirebase")
    fun updateDataFromFirebase() = firebaseService.updateDataFromFirebase()

    @GetMapping("/updateFromFirebaseFromStartKeyForLang")
    fun updateDataFromFirebaseFromStartKeyForLang(
            @RequestParam(value = "startKey", defaultValue = "") startKey: String,
            @RequestParam(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance
    ) = firebaseService.updateDataFromFirebase(startKey, lang)

    @GetMapping("/updateDataDates")
    fun getUpdateDataDates() = firebaseService.getAllFirebaseUpdatedDataDates()
}
