package ru.kuchanov.scpreaderapi.controller.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidSubscription
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjection
import ru.kuchanov.scpreaderapi.model.user.LeaderboardUserDto
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.UserAndroidPurchaseService
import ru.kuchanov.scpreaderapi.service.users.UserService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.USER)
class UserController @Autowired constructor(
        val userService: UserService,
        val userAndroidPurchaseService: UserAndroidPurchaseService
) {

    @GetMapping("/me")
    fun showMe(@AuthenticationPrincipal user: User): UserProjection =
            userService.getByIdAsDto(user.id!!) ?: throw UserNotFoundException()

    @PostMapping("/edit")
    fun editAccount(
            @AuthenticationPrincipal user: User,
            @RequestParam(value = "name") name: String,
            @RequestParam(value = "avatarUrl") avatarUrl: String
    ): UserProjection =
            userService.editAccount(user.id!!, name, avatarUrl)

    @GetMapping("/leaderboard")
    fun getUsersForLangWithLimitAndOffsetSortedByScore(
            @RequestParam(value = "offset") offset: Int,
            @RequestParam(value = "limit") limit: Int
    ): List<LeaderboardUserDto> =
            userService.getLeaderboardUsersByLangWithOffsetAndLimitSortedByScore(offset, limit)

    @GetMapping("/{lang}/count")
    fun getUsersCountForLang(
            @PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance
    ): Long =
            userService.getUsersByLangIdCount(lang.lang)

    @GetMapping("/{lang}/leaderboard/{userId}")
    fun getUserPositionInLeaderboardForLang(
            @PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance,
            @PathVariable(value = "userId") userId: Long
    ): Int =
            userService.getUserPositionInLeaderboard(userId, lang.lang)

    @GetMapping("/{lang}/leaderboard/position")
    fun getCurrentUserPositionInLeaderboardForLang(
            @PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance,
            @AuthenticationPrincipal user: User
    ): Int =
            userService.getUserPositionInLeaderboard(user.id!!, lang.lang)

    @GetMapping("/android/product/all")
    fun showAndroidProducts(@AuthenticationPrincipal user: User): List<AndroidProduct> =
            userAndroidPurchaseService.findAllProducts(user.id!!)

    @GetMapping("/android/subscription/all")
    fun showAndroidSubscriptions(@AuthenticationPrincipal user: User): List<AndroidSubscription> =
            userAndroidPurchaseService.findAllSubscriptions(user.id!!)
}
