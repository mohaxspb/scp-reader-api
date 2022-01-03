package ru.kuchanov.scpreaderapi.configuration.security

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator
import org.springframework.security.oauth2.provider.token.TokenStore
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserService


@Configuration
@EnableAuthorizationServer
class AuthorizationServerConfiguration @Autowired constructor(
    private val log: Logger,
    private val tokenStore: TokenStore,
    private val clientDetailsService: ClientDetailsService,
    private val userDetailsService: ScpReaderUserService,
    private val authenticationManager: AuthenticationManager
) : AuthorizationServerConfigurerAdapter() {

    override fun configure(clients: ClientDetailsServiceConfigurer) {
        clients.withClientDetails(clientDetailsService)
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        endpoints.tokenStore(tokenStore)
            .authenticationManager(authenticationManager)
            .userDetailsService(userDetailsService)
            .exceptionTranslator(object : DefaultWebResponseExceptionTranslator() {
                override fun translate(e: Exception): ResponseEntity<OAuth2Exception> {
                    log.error("exceptionTranslator", e)
                    e.printStackTrace()

                    val responseEntity: ResponseEntity<OAuth2Exception> = super.translate(e)
                    return ResponseEntity(responseEntity.body, responseEntity.headers, responseEntity.statusCode)
                }
            })
    }
}