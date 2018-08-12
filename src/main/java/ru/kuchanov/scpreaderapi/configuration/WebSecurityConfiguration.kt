package ru.kuchanov.scpreaderapi.configuration

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
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import ru.kuchanov.scpreaderapi.service.auth.ClientServiceImpl
import ru.kuchanov.scpreaderapi.service.auth.UserServiceImpl
import javax.servlet.Filter


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class WebSecurityConfiguration : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var clientDetailsService: ClientServiceImpl

    @Autowired
    lateinit var tokenServices: ResourceServerTokenServices

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val authenticationProvider = DaoAuthenticationProvider()
        authenticationProvider.setUserDetailsService(userDetailsService)
        authenticationProvider.setPasswordEncoder(passwordEncoder())

        return authenticationProvider
    }

    @Primary
    @Bean
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

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
    fun oauth2authenticationManager(): OAuth2AuthenticationManager {
        val authManager = OAuth2AuthenticationManager()
        authManager.setClientDetailsService(clientDetailsService)
        authManager.setTokenServices(tokenServices)

        return authManager
    }

    @Bean
    fun myOAuth2Filter(): Filter {
        val filter = OAuth2AuthenticationProcessingFilter()
        filter.setAuthenticationManager(oauth2authenticationManager())
        //allow auth with cookies (not only with token)
        filter.setStateless(false)

        return filter
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
                .anyRequest()
                .authenticated()
        http
                .formLogin()
                .successHandler { request, response, authentication ->
                    println("angular.port: ${angularServerPort}")
                    println("request: ${request.localName}/${request.localAddr}/${request.localPort}/${request.serverName}")
                    DefaultRedirectStrategy().sendRedirect(request, response, "http://${request.serverName}:$angularServerPort");
                }
                .and()
                .logout()
                .logoutSuccessHandler { request, response, authentication ->
                    println("angular.port: ${angularServerPort}")
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
        web.ignoring().antMatchers("*.bundle.*")
    }

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