package ru.kuchanov.scpreaderapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.users.UserService


@RestController
class IndexController {

    @Autowired
    lateinit var userService: UserService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @GetMapping("/")
    fun index(): String = "Greetings from Spring Boot!"

    @GetMapping("/me")
    fun showMe(
            @AuthenticationPrincipal user: User,
            @RequestParam(value = "showFull") showFull: Boolean = true
    ) = if (showFull) userService.getById(user.id!!) else user

    @GetMapping("/encrypt")
    fun encrypt(@RequestParam(value = "target") target: String): String =
            passwordEncoder.encode(target)
}
