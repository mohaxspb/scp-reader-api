package ru.kuchanov.scpreaderapi.controller

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.Constants
import ru.kuchanov.scpreaderapi.service.firebase.FirebaseService
import ru.kuchanov.scpreaderapi.service.users.UserService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore


@RestController
@RequestMapping("/${Constants.Path.AUTH}")
class AuthController {

    @Autowired
    private lateinit var firebaseService: FirebaseService

    @Autowired
    private lateinit var usersService: UserService

    @Autowired
    private lateinit var oauth2authenticationManager: OAuth2AuthenticationManager

    //todo think if we need it
    @Autowired
    private lateinit var authenticationProvider: DaoAuthenticationProvider

    @Autowired
    private lateinit var tokenStore: JdbcTokenStore

    @Autowired
    private lateinit var googleIdTokenVerifier: GoogleIdTokenVerifier

    @PostMapping("/socialLogin")
    fun authorize(
            @RequestParam(value = "provider") provider: Constants.SocialProvider,
            @RequestParam(value = "token") token: String
    ): String? {
        println("authorize called")

        when (provider) {
            Constants.SocialProvider.GOOGLE -> {
                val googleIdToken = googleIdTokenVerifier.verify(token)
                println(googleIdToken)

//                val auth = Authentication()
//                oauth2authenticationManager.authenticate(auth)

                val oauth2Token = tokenStore.findTokensByUserName(googleIdToken.payload.email)
                println("oauth2Token: $oauth2Token")

//                tokenStore.

                val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(
                        googleIdToken.payload.email,
                        googleIdToken.payload.email,
                        listOf(SimpleGrantedAuthority("USER"))
                )
                println("token: $usernamePasswordAuthenticationToken")
//                token.details = WebAuthenticationDetails(request)//if request is needed during authentication
                val auth: Authentication
                try {
                    auth = oauth2authenticationManager.authenticate(usernamePasswordAuthenticationToken)
                    println("auth: $auth")
                } catch (e: AuthenticationException) {
                    //if failureHandler exists
                    throw e
                }

                val securityContext = SecurityContextHolder.getContext()
                securityContext.authentication = auth

                return usernamePasswordAuthenticationToken.toString()
            }
            else -> throw IllegalArgumentException("Unexpected provider: $provider")
        }

//        return null

//        val oAuth2AccessToken = null
//        return oAuth2AccessToken
    }
}