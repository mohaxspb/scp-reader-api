package ru.kuchanov.scpreaderapi.controller

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.monetization.purchase.android.UserAndroidPurchaseService
import ru.kuchanov.scpreaderapi.service.users.UserService


@RestController
@RequestMapping("/${ScpReaderConstants.Path.USER}")
class UserController {

    @Autowired
    private lateinit var log: Logger

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var userAndroidPurchaseService: UserAndroidPurchaseService

    @GetMapping("/me")
    fun showMe(
            @AuthenticationPrincipal user: User,
            @RequestParam(value = "showFull") showFull: Boolean = false
    ) = if (showFull) userService.getById(user.id!!) else user

    @GetMapping("/android/product/all")
    fun showAndroidProducts(@AuthenticationPrincipal user: User) =
            userAndroidPurchaseService.findAllProducts(user.id!!)

    @GetMapping("/android/subscription/all")
    fun showAndroidSubscriptions(@AuthenticationPrincipal user: User) =
            userAndroidPurchaseService.findAllSubscriptions(user.id!!)
}