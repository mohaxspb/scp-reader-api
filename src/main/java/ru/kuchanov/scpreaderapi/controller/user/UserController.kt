package ru.kuchanov.scpreaderapi.controller.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct
import ru.kuchanov.scpreaderapi.bean.push.UsersToPushTokens
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjection
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjectionV2
import ru.kuchanov.scpreaderapi.model.user.LeaderboardUserDto
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.UserAndroidPurchaseService
import ru.kuchanov.scpreaderapi.service.push.UserToPushTokensService
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserService
import java.time.temporal.ChronoUnit


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.USER)
class UserController @Autowired constructor(
    private val scpReaderUserService: ScpReaderUserService,
    private val userToPushTokensService: UserToPushTokensService
) {

    @Deprecated("Uses deprecated return type", ReplaceWith("showMeV2"))
    @GetMapping("/me")
    fun showMe(@AuthenticationPrincipal user: User): UserProjection =
            scpReaderUserService.getByIdAsDto(user.id!!) ?: throw UserNotFoundException()

    @GetMapping("/me/v2")
    fun showMeV2(@AuthenticationPrincipal user: User): UserProjectionV2 =
            scpReaderUserService.getByIdAsDtoV2(user.id!!) ?: throw UserNotFoundException()

    @Deprecated("Uses deprecated return type", ReplaceWith("edit/v2"))
    @PostMapping("/edit")
    fun editAccount(
            @AuthenticationPrincipal user: User,
            @RequestParam(value = "name") name: String,
            @RequestParam(value = "avatarUrl") avatarUrl: String
    ): UserProjection =
            scpReaderUserService.editAccount(user.id!!, name, avatarUrl)

    @PostMapping("/edit/v2")
    fun editAccountV2(
            @AuthenticationPrincipal user: User,
            @RequestParam(value = "name") name: String,
            @RequestParam(value = "avatarUrl") avatarUrl: String
    ): UserProjectionV2 =
            scpReaderUserService.editAccountV2(user.id!!, name, avatarUrl)

    @PostMapping("/disableAdsAndOfflineLimit")
    fun disableAdsAndOfflineLimit(
            @AuthenticationPrincipal user: User,
            @RequestParam targetUserId: Long,
            @RequestParam disableAds: Boolean,
            @RequestParam disableOfflineLimit: Boolean,
            @RequestParam period: Int,
            @RequestParam timeUnit: ChronoUnit
    ): UserProjectionV2 =
            scpReaderUserService.disableAdsAndOfflineLimit(
                    targetUserId,
                    disableAds,
                    disableOfflineLimit,
                    period,
                    timeUnit
            )

    @GetMapping("/leaderboard")
    fun getUsersForLangWithLimitAndOffsetSortedByScore(
            @RequestParam(value = "offset") offset: Int,
            @RequestParam(value = "limit") limit: Int
    ): List<LeaderboardUserDto> =
            scpReaderUserService.getLeaderboardUsersByLangWithOffsetAndLimitSortedByScore(offset, limit)

    @GetMapping("/{lang}/count")
    fun getUsersCountForLang(
            @PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance
    ): Long =
            scpReaderUserService.getUsersByLangIdCount(lang.lang)

    @GetMapping("/{lang}/leaderboard/{userId}")
    fun getUserPositionInLeaderboardForLang(
            @PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance,
            @PathVariable(value = "userId") userId: Long
    ): Int =
            scpReaderUserService.getUserPositionInLeaderboard(userId, lang.lang)

    @GetMapping("/{lang}/leaderboard/position")
    fun getCurrentUserPositionInLeaderboardForLang(
            @PathVariable(value = "lang") lang: ScpReaderConstants.Firebase.FirebaseInstance,
            @AuthenticationPrincipal user: User
    ): Int =
            scpReaderUserService.getUserPositionInLeaderboard(user.id!!, lang.lang)

    @PostMapping("/push/token/{provider}")
    fun receiveUserPushToken(
            @PathVariable(value = "provider") provider: ScpReaderConstants.Push.Provider,
            @RequestParam(value = "pushToken") pushToken: String,
            @AuthenticationPrincipal user: User
    ): UsersToPushTokens {
        checkNotNull(user.id)
        val tokenToUserConnectionToUpdate = userToPushTokensService
                .findByUserIdAndPushTokenProviderAndPushTokenValue(
                        user.id,
                        provider,
                        pushToken
                )
                ?: UsersToPushTokens(
                        userId = user.id,
                        pushTokenValue = pushToken,
                        pushTokenProvider = provider
                )

        return userToPushTokensService.save(tokenToUserConnectionToUpdate)
    }
}
