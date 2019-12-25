package ru.kuchanov.scpreaderapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class IndexController @Autowired constructor(
        val passwordEncoder: PasswordEncoder
) {

    @GetMapping("/")
    fun index(): String = "Greetings from Spring Boot!"

    @GetMapping("/encrypt")
    fun encrypt(@RequestParam(value = "target") target: String): String =
            passwordEncoder.encode(target)
}
