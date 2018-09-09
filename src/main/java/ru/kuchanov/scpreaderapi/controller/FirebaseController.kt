package ru.kuchanov.scpreaderapi.controller

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.Constants
import ru.kuchanov.scpreaderapi.model.user.LeaderboardUser
import ru.kuchanov.scpreaderapi.network.ApiClient
import ru.kuchanov.scpreaderapi.network.ModelConverter
import ru.kuchanov.scpreaderapi.service.firebase.FirebaseService
import ru.kuchanov.scpreaderapi.service.users.UserService


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

    @Autowired
    private lateinit var usersService: UserService

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

    @GetMapping("/{lang}/users/leaderboard")
    fun getUsersForLangWithLimitAndOffsetSortedByScore(
            @PathVariable(value = "lang") lang: Constants.Firebase.FirebaseInstance,
            @RequestParam(value = "offset") offset: Int,
            @RequestParam(value = "limit") limit: Int
    ) = usersService.getUsersByLangWithOffsetAndLimitSortedByScore(lang.lang, offset, limit).map {
        LeaderboardUser(
                id = it.id!!,
                avatar = it.avatar!!,
                fullName = it.fullName!!,
                score = it.score!!,
                //level
                levelNum = it.levelNum!!,
                scoreToNextLevel = it.scoreToNextLevel!!,
                curLevelScore = it.curLevelScore!!
        )
    }
}