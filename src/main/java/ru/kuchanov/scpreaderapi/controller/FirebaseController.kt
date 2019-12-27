package ru.kuchanov.scpreaderapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.service.firebase.FirebaseService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.FIREBASE)
class FirebaseController @Autowired constructor(
        val firebaseService: FirebaseService
) {

    @GetMapping("/updateFromFirebase")
    fun updateDataFromFirebase() = firebaseService.updateDataFromFirebase()

    @GetMapping("/{langEnum}/updateFromFirebaseFromStartKey")
    fun updateDataFromFirebaseFromStartKeyForLang(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "startKey", defaultValue = "") startKey: String,
            @RequestParam(value = "maxUsersCount") maxUsersCount: Int?
    ) = firebaseService.updateDataFromFirebase(startKey, langEnum, maxUsersCount)

    @GetMapping("/updateDataDates")
    fun getUpdateDataDates() = firebaseService.getAllFirebaseUpdatedDataDates()
}
