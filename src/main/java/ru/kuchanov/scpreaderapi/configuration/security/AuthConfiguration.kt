package ru.kuchanov.scpreaderapi.configuration.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.crypto.password.PasswordEncoder
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserService


@Configuration
class AuthConfiguration @Autowired constructor(
    private val userDetailsService: ScpReaderUserService,
    private val passwordEncoder: PasswordEncoder,
) {

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider =
        DaoAuthenticationProvider().apply {
            setUserDetailsService(userDetailsService)
            setPasswordEncoder(passwordEncoder)
        }
}