package ru.kuchanov.scpreaderapi.controller

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.Constants
import ru.kuchanov.scpreaderapi.service.firebase.FirebaseService
import ru.kuchanov.scpreaderapi.service.users.UserService
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.util.HashSet
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.GrantedAuthority
import ru.kuchanov.scpreaderapi.bean.users.User
import java.util.HashMap
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices
import java.io.Serializable
import org.springframework.security.oauth2.provider.token.DefaultTokenServices




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
    private lateinit var tokenEndpoint: TokenEndpoint


    @Autowired
    private lateinit var tokenServices: AuthorizationServerTokenServices

    @Autowired
    private lateinit var googleIdTokenVerifier: GoogleIdTokenVerifier

    @PostMapping("/socialLogin")
    fun authorize(
            @RequestParam(value = "provider") provider: Constants.SocialProvider,
            @RequestParam(value = "token") token: String
    ): OAuth2AccessToken? {
        println("authorize called")

        when (provider) {
            Constants.SocialProvider.GOOGLE -> {
                val googleIdToken = googleIdTokenVerifier.verify(token)
                println(googleIdToken)

                return getAccessToken(googleIdToken.payload.email)
            }
            else -> throw IllegalArgumentException("Unexpected provider: $provider")
        }
    }

    fun getAccessToken(email: String): OAuth2AccessToken? {
        val authorities = HashSet<GrantedAuthority>()
        authorities.add(SimpleGrantedAuthority("ROLE_USER"))

        val requestParameters = HashMap<String, String>()
        val clientId = "client_id"
        val approved = true
        val scope = HashSet<String>()
        scope.add("read write")
        val resourceIds = HashSet<String>()
        val responseTypes = HashSet<String>()
        responseTypes.add("code")
        val extensionProperties = HashMap<String, Serializable>()

        val oAuth2Request = OAuth2Request(
                requestParameters,
                clientId,
                authorities,
                approved,
                scope,
                resourceIds,
                null,
                responseTypes,
                extensionProperties
        )

        val authenticationToken = UsernamePasswordAuthenticationToken(usersService.loadUserByUsername(email), null, authorities)
        val auth = OAuth2Authentication(oAuth2Request, authenticationToken)
        println("auth: $auth")
        return tokenServices.createAccessToken(auth)
    }
}