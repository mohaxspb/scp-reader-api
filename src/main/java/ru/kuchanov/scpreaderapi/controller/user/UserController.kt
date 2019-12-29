package ru.kuchanov.scpreaderapi.controller.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.UserAndroidPurchaseService
import ru.kuchanov.scpreaderapi.service.users.UserService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.USER)
class UserController @Autowired constructor(
        val userService: UserService,
        val userAndroidPurchaseService: UserAndroidPurchaseService
) {

    @GetMapping("/me")
    fun showMe(
            @AuthenticationPrincipal user: User,
            @RequestParam(value = "showFull") showFull: Boolean = false
    ) = if (showFull) userService.getById(user.id!!) else user

    @GetMapping("/{lang}/leaderboard")
    fun getUsersForLangWithLimitAndOffsetSortedByScore(
            @PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "offset") offset: Int,
            @RequestParam(value = "limit") limit: Int
    ) = userService.getLeaderboardUsersByLangWithOffsetAndLimitSortedByScore(lang.lang, offset, limit)

    @GetMapping("/{lang}/count")
    fun getUsersCountForLang(@PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance) =
            userService.getUsersByLangIdCount(lang.lang)

    @GetMapping("/{lang}/leaderboard/{userId}")
    fun getUserPositionInLeaderboardForLang(
            @PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance,
            @PathVariable(value = "userId") userId: Long
    ): Int = userService.getUserPositionInLeaderboard(userId, lang.lang)

    @GetMapping("/{lang}/leaderboard/position")
    fun getCurrentUserPositionInLeaderboardForLang(
            @PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance,
            @AuthenticationPrincipal user: User
    ): Int = userService.getUserPositionInLeaderboard(user.id!!, lang.lang)

    @GetMapping("/android/product/all")
    fun showAndroidProducts(@AuthenticationPrincipal user: User) =
            userAndroidPurchaseService.findAllProducts(user.id!!)

    @GetMapping("/android/subscription/all")
    fun showAndroidSubscriptions(@AuthenticationPrincipal user: User) =
            userAndroidPurchaseService.findAllSubscriptions(user.id!!)
}
