package ru.kuchanov.scpreaderapi.controller

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.Application
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.isAdmin
import ru.kuchanov.scpreaderapi.configuration.CacheService
import ru.kuchanov.scpreaderapi.model.exception.ScpAccessDeniedException
import ru.kuchanov.scpreaderapi.service.mail.MailService


@RestController
class IndexController @Autowired constructor(
    private val log: Logger,
    private val passwordEncoder: PasswordEncoder,
    private val mailService: MailService,
    private val cacheService: CacheService,
    @Qualifier(Application.CACHE_LOGGER) private val cacheLog: Logger
) {

    @GetMapping("/")
    fun index(): String = "Welcome to ScpReader API!"

    @GetMapping("/populateCache")
    fun populateCache(@AuthenticationPrincipal user: User): String {
        return if (user.isAdmin()) {
            cacheService.populateCache()

            "Populate cache started"
        } else {
            throw ScpAccessDeniedException()
        }
    }

    @GetMapping("/populateCacheStatus")
    fun populateCacheStatus(@AuthenticationPrincipal user: User): CacheService.CacheStatus {
        return if (user.isAdmin()) {
            cacheService.cacheStatus.get()
        } else {
            throw ScpAccessDeniedException()
        }
    }

    @Scheduled(
        /**
         * second, minute, hour, day, month, day of week
         *
         * Each 5 minutes in interval of 1-59 minutes
         */
        cron = "0 1-59/5 * * * *"
    )
    fun populateCacheJob() {
        when (cacheService.cacheStatus.get()) {
            CacheService.CacheStatus.NotPopulated -> {
                cacheLog.debug("Cache not populated, so start populating.")
                cacheService.populateCache()
            }
            is CacheService.CacheStatus.Populated -> cacheLog.debug("Cache already populated, do nothing.")
            CacheService.CacheStatus.Populating -> cacheLog.debug("Cache is populating now, do nothing.")
        }
    }

    @GetMapping("/encrypt")
    fun encrypt(@RequestParam(value = "target") target: String): String =
        passwordEncoder.encode(target)


    @Deprecated("just for tests")
    @GetMapping("/securedAdmin")
    fun securedAdmin(@AuthenticationPrincipal user: User): String =
        "Secured for admin use only! Admin:\n\n\n$user"

    @Deprecated("just for tests")
    @GetMapping("/securedNotAdmin")
    fun securedNotAdmin(@AuthenticationPrincipal user: User): String =
        "Secured for not admin use only! Admin:\n\n\n$user"

    @GetMapping("/sendStatisticsEmail")
    fun sendStatisticsEmail(
        @AuthenticationPrincipal user: User,
        @RequestParam today: Boolean
    ): String {
        log.error("sendStatisticsEmail: $user")
        return if (user.isAdmin()) {
            if (today) {
                mailService.sendStatisticsEmail(true)
            } else {
                mailService.sendStatisticsEmail(false)
            }
            "Statistics email sent successfully!"
        } else {
            throw ScpAccessDeniedException()
        }
    }
}
