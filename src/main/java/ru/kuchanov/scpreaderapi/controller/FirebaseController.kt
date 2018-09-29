package ru.kuchanov.scpreaderapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.Constants
import ru.kuchanov.scpreaderapi.service.firebase.FirebaseService
import ru.kuchanov.scpreaderapi.service.users.UserService


@RestController
@RequestMapping("/${Constants.Path.FIREBASE}")
class FirebaseController {

    @Autowired
    private lateinit var firebaseService: FirebaseService

    @Autowired
    private lateinit var usersService: UserService

    @Scheduled(
            fixedDelay = Constants.FIREBASE_USERS_DATA_UPDATE_RATE_MILLIS,
            initialDelay = Constants.FIREBASE_USERS_DATA_UPDATE_RATE_MILLIS
    )
    @GetMapping("/users/updateFromFirebase")
    fun updateDataFromFirebase() = firebaseService.updateDataFromFirebase()

    @GetMapping("/users/updateFromFirebaseFromStartKeyForLang")
    fun updateDataFromFirebaseFromStartKeyForLang(
            @RequestParam(value = "startKey", defaultValue = "") startKey: String,
            @RequestParam(value = "lang") lang: Constants.Firebase.FirebaseInstance
    ) = firebaseService.updateDataFromFirebase(startKey, lang)

    @GetMapping("/{lang}/users/all")
    fun getAllUsersForLang(@PathVariable(value = "lang") lang: Constants.Firebase.FirebaseInstance) =
            firebaseService.getAllUsersForLang(lang.lang)

    @GetMapping("/{lang}/users/count")
    fun getUsersCountForLang(@PathVariable(value = "lang") lang: Constants.Firebase.FirebaseInstance) =
            firebaseService.getUsersByLangIdCount(lang.lang)

    @GetMapping("/{lang}/users/leaderboard")
    fun getUsersForLangWithLimitAndOffsetSortedByScore(
            @PathVariable(value = "lang") lang: Constants.Firebase.FirebaseInstance,
            @RequestParam(value = "offset") offset: Int,
            @RequestParam(value = "limit") limit: Int
    ) = usersService.getLeaderboardUsersByLangWithOffsetAndLimitSortedByScore(lang.lang, offset, limit)

    @GetMapping("/updateDataDates")
    fun getUpdateDataDates() = firebaseService.getAllFirebaseUpdatedDataDates()
}