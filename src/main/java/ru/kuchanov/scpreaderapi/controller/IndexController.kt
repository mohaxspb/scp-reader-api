package ru.kuchanov.scpreaderapi.controller

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.isAdmin
import ru.kuchanov.scpreaderapi.model.exception.ScpAccessDeniedException
import ru.kuchanov.scpreaderapi.service.mail.MailService


@RestController
class IndexController @Autowired constructor(
        val log: Logger,
        val passwordEncoder: PasswordEncoder,
        val mailService: MailService
) {

    @GetMapping("/")
    fun index(): String = "Welcome to ScpReader API!"

    @GetMapping("/encrypt")
    fun encrypt(@RequestParam(value = "target") target: String): String =
            passwordEncoder.encode(target)

    @GetMapping("/sendStatisticsEmail")
    fun sendStatisticsEmail(@AuthenticationPrincipal user: User): String {
        log.error("sendStatisticsEmail: $user")
        return if (user.isAdmin()) {
            mailService.sendStatisticsEmail()
            "Statistics email sent successfully!"
        } else {
            throw ScpAccessDeniedException()
        }
    }

}
