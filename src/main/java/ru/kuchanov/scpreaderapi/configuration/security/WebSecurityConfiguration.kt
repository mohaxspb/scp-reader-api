package ru.kuchanov.scpreaderapi.configuration.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.service.auth.ClientServiceImpl
import ru.kuchanov.scpreaderapi.service.users.UserServiceImpl
import javax.servlet.Filter


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class WebSecurityConfiguration : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var clientDetailsService: ClientServiceImpl

    @Autowired
    private lateinit var tokenStore: TokenStore

    @Bean
    fun tokenServices() = DefaultTokenServices().apply {
        setTokenStore(tokenStore)
        setClientDetailsService(clientDetailsService)
        setAuthenticationManager(authenticationManager())
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider =
            DaoAuthenticationProvider().apply {
                setUserDetailsService(userDetailsService)
                setPasswordEncoder(passwordEncoder())
            }

    @Primary
    @Bean
    override fun authenticationManagerBean(): AuthenticationManager =
            super.authenticationManagerBean()

    @Autowired
    lateinit var userDetailsService: UserServiceImpl

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth
                .authenticationProvider(authenticationProvider())
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
    }

    @Bean
    fun oauth2authenticationManager(): OAuth2AuthenticationManager =
            OAuth2AuthenticationManager().apply {
                setClientDetailsService(clientDetailsService)
                setTokenServices(tokenServices())
            }

    @Bean
    fun myOAuth2Filter(): Filter =
            OAuth2AuthenticationProcessingFilter().apply {
                setAuthenticationManager(oauth2authenticationManager())
                //allow auth with cookies (not only with token)
                setStateless(false)
            }

    @Value("\${angular.port}")
    lateinit var angularServerPort: String

    @Value("\${angular.href}")
    lateinit var angularServerHref: String

    override fun configure(http: HttpSecurity) {
        http
                .cors()
        http
                .csrf()
                .disable()
        http
                .authorizeRequests()
                .antMatchers("/", "/login**", "/error**")
                .permitAll()
        http
                .authorizeRequests()
                .anyRequest()
                .authenticated()

        http
                .formLogin()
                .successHandler { request, response, _ ->
                    println("request data: ${request.localName}/${request.localAddr}/${request.localPort}/${request.serverName}")
                    println("request: ${request.remotePort}")
                    println("request: ${request.serverPort}")
                    println("request: ${request.scheme}")
                    DefaultRedirectStrategy().sendRedirect(
                            request,
                            response,
                            "${request.scheme}://${request.serverName}$angularServerPort$angularServerHref"
                    )
                }
                .and()
                .logout()
                .logoutSuccessHandler { request, response, _ ->
                    println("request data: ${request.localName}/${request.localAddr}/${request.localPort}/${request.serverName}")
                    println("request: ${request.protocol}")
                    println("request: ${request.scheme}")
                    DefaultRedirectStrategy().sendRedirect(
                            request,
                            response,
                            "${request.scheme}://${request.serverName}$angularServerPort$angularServerHref"
                    )
                }
                .permitAll()

        http
                .addFilterBefore(
                        myOAuth2Filter(),
                        BasicAuthenticationFilter::class.java
                )
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers(
                "/image/**",
                //todo remove. Allow only admin access on PROD
                "/${ScpReaderConstants.Path.FIREBASE}/**/**/**",
                "/${ScpReaderConstants.Path.AUTH}/**",
                "/${ScpReaderConstants.Path.PURCHASE}/**",
                "/${ScpReaderConstants.Path.ADS}/all",
                "/${ScpReaderConstants.Path.ADS}/files/**",
                "/${ScpReaderConstants.Path.ARTICLE}/**/recent/**",
                "/${ScpReaderConstants.Path.ARTICLE}/**/rated/**",
                "/${ScpReaderConstants.Path.ARTICLE}/**/object/**",
                "/${ScpReaderConstants.Path.ARTICLE}/**/category/**",
                "/${ScpReaderConstants.Path.ARTICLE}/**/full",
                //todo remove. Allow only admin access on PROD
                "/${ScpReaderConstants.Path.ARTICLE}/**/delete",
                //todo remove. Allow only admin access on PROD
                "/${ScpReaderConstants.Path.ARTICLE}/${ScpReaderConstants.Path.PARSE}/**"
        )
    }
}
