package ru.kuchanov.scpreaderapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.firebase.FirebaseService
import ru.kuchanov.scpreaderapi.service.users.UserService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.FIREBASE)
class FirebaseController @Autowired constructor(
        val firebaseService: FirebaseService,
        val usersService: UserService
) {

    @GetMapping("/users/updateFromFirebase")
    fun updateDataFromFirebase() = firebaseService.updateDataFromFirebase()

    @GetMapping("/users/updateFromFirebaseFromStartKeyForLang")
    fun updateDataFromFirebaseFromStartKeyForLang(
            @RequestParam(value = "startKey", defaultValue = "") startKey: String,
            @RequestParam(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance
    ) = firebaseService.updateDataFromFirebase(startKey, lang)

    @GetMapping("/{lang}/users/all")
    fun getAllUsersForLang(@PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance) =
            firebaseService.getAllUsersForLang(lang.lang)

    @GetMapping("/{lang}/users/count")
    fun getUsersCountForLang(@PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance) =
            firebaseService.getUsersByLangIdCount(lang.lang)

    @GetMapping("/{lang}/users/leaderboard")
    fun getUsersForLangWithLimitAndOffsetSortedByScore(
            @PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "offset") offset: Int,
            @RequestParam(value = "limit") limit: Int
    ) = usersService.getLeaderboardUsersByLangWithOffsetAndLimitSortedByScore(lang.lang, offset, limit)

    @GetMapping("/{lang}/users/leaderboard/{userId}")
    fun getUserPositionInLeaderboardForLang(
            @PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance,
            @PathVariable(value = "userId") userId: Long
    ): Int = usersService.getUserPositionInLeaderboard(userId, lang.lang)

    @GetMapping("/{lang}/users/leaderboard/position")
    fun getCurrentUserPositionInLeaderboardForLang(
            @PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance,
            @AuthenticationPrincipal user: User
    ): Int = usersService.getUserPositionInLeaderboard(user.id!!, lang.lang)

    @GetMapping("/updateDataDates")
    fun getUpdateDataDates() = firebaseService.getAllFirebaseUpdatedDataDates()
}
