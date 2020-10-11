package ru.kuchanov.scpreaderapi.configuration.security

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore
import ru.kuchanov.scpreaderapi.service.auth.ClientServiceImpl
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserServiceImpl
import javax.sql.DataSource


@Configuration
@EnableAuthorizationServer
class AuthorizationServerConfiguration : AuthorizationServerConfigurerAdapter() {

    @Autowired
    private lateinit var dataSource: DataSource

    @Autowired
    private lateinit var log: Logger

    @Bean
    fun tokenStore(): TokenStore = JdbcTokenStore(dataSource)

    @Autowired
    private lateinit var clientDetailsService: ClientServiceImpl

    @Autowired
    private lateinit var userDetailsService: ScpReaderUserServiceImpl

    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    override fun configure(clients: ClientDetailsServiceConfigurer) {
        clients
                .withClientDetails(clientDetailsService)
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        endpoints.tokenStore(tokenStore())
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