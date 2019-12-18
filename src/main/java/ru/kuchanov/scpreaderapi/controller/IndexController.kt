package ru.kuchanov.scpreaderapi.controller

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.auth.AuthorityService
import ru.kuchanov.scpreaderapi.service.users.UserService


@RestController
class IndexController {

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var authoritiesService: AuthorityService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Suppress("unused")
    @Autowired
    private lateinit var log: Logger

    @GetMapping("/")
    fun index(): String = "Greetings from Spring Boot!"

    @GetMapping("/hello")
    fun test(@RequestParam(value = "name", defaultValue = "World") name: String) = "Hello, $name"

    @GetMapping("/showUsers")
    fun showUsers() = userService.findAll()

    @GetMapping("/me")
    fun showMe(
            @AuthenticationPrincipal user: User,
            @RequestParam(value = "showFull") showFull: Boolean = true
    ) = if (showFull) userService.getById(user.id!!) else user

    @GetMapping("/showAuthorities")
    fun showAuthorities() = authoritiesService.findAll()

    @GetMapping("/encrypt")
    fun encrypt(@RequestParam(value = "target") target: String): String =
            passwordEncoder.encode(target)
}