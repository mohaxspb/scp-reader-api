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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
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
    fun authenticationProvider(): DaoAuthenticationProvider = DaoAuthenticationProvider().apply {
        setUserDetailsService(userDetailsService)
        setPasswordEncoder(passwordEncoder())
    }

    @Primary
    @Bean
    override fun authenticationManagerBean(): AuthenticationManager = super.authenticationManagerBean()

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
    fun oauth2authenticationManager(): OAuth2AuthenticationManager = OAuth2AuthenticationManager().apply {
        setClientDetailsService(clientDetailsService)
        setTokenServices(tokenServices())
    }

    @Bean
    fun myOAuth2Filter(): Filter = OAuth2AuthenticationProcessingFilter().apply {
        setAuthenticationManager(oauth2authenticationManager())
        //allow auth with cookies (not only with token)
        setStateless(false)
    }

    @Value("\${angular.port}")
    lateinit var angularServerPort: String

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
                    println("angular.port: $angularServerPort")
                    println("request: ${request.localName}/${request.localAddr}/${request.localPort}/${request.serverName}")
                    DefaultRedirectStrategy().sendRedirect(request, response, "http://${request.serverName}:$angularServerPort");
                }
                .and()
                .logout()
                .logoutSuccessHandler { request, response, _ ->
                    println("angular.port: $angularServerPort")
                    println("request: ${request.localName}/${request.localAddr}/${request.localPort}/${request.serverName}")
                    DefaultRedirectStrategy().sendRedirect(request, response, "http://${request.serverName}:$angularServerPort");
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
                "/${ScpReaderConstants.Path.GALLERY}/files/**",
                "/${ScpReaderConstants.Path.GALLERY}/all",
                "/${ScpReaderConstants.Path.FIREBASE}/**/**/**",
                "/${ScpReaderConstants.Path.AUTH}/**",
                "/${ScpReaderConstants.Path.PURCHASE}/**"
        )
    }

    //todo check if we really need it
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf(
                "HEAD",
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH"
        )
        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
        configuration.allowCredentials = true
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.allowedHeaders = listOf(
                "Authorization",
                "Cache-Control",
                "Content-Type"
        )
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}