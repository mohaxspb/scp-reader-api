package ru.kuchanov.scpreaderapi.controller.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Score.DEFAULT_SCORE_FOR_REWARDED_VIDEO
import ru.kuchanov.scpreaderapi.bean.transaction.UserDataTransaction
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.model.dto.monetization.AddUserDataTransactionResultDto
import ru.kuchanov.scpreaderapi.service.transaction.UserDataTransactionService
import ru.kuchanov.scpreaderapi.service.users.UserService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.USER + "/" + ScpReaderConstants.Path.TRANSACTION)
class UserTransactionController @Autowired constructor(
        val userService: UserService,
        val userDataTransactionService: UserDataTransactionService
) {

    @PostMapping("/rewardedVideo/add")
    fun getCurrentUserPositionInLeaderboardForLang(
            @RequestParam("adsProvider") adsProvider: String,
            @AuthenticationPrincipal user: User
    ): AddUserDataTransactionResultDto {
        val transaction = userDataTransactionService.save(
                UserDataTransaction(
                        userId = user.id!!,
                        transactionType = ScpReaderConstants.UserDataTransactionType.REWARDED_VIDEO,
                        scoreAmount = DEFAULT_SCORE_FOR_REWARDED_VIDEO,
                        transactionData = adsProvider
                )
        )

        val userInDb = userService.getById(user.id) ?: throw UserNotFoundException()
        val updatedUser = userService.save(userInDb.apply { score += transaction.scoreAmount })

        return AddUserDataTransactionResultDto(
                userDataTransaction = transaction,
                totalUserScore = updatedUser.score
        )
    }
}
