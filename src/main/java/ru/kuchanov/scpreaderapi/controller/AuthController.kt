package ru.kuchanov.scpreaderapi.controller

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.Constants
import ru.kuchanov.scpreaderapi.service.firebase.FirebaseService
import ru.kuchanov.scpreaderapi.service.users.UserService
import java.io.Serializable
import java.util.*
import java.util.HashSet
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.provider.OAuth2Authentication
import java.util.HashMap




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
    private lateinit var tokenServices: DefaultTokenServices

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
                val googleIdToken: GoogleIdToken? = googleIdTokenVerifier.verify(token)
                println(googleIdToken)

                return googleIdToken?.let { getAccessToken(it.payload.email) }
                        ?: throw IllegalStateException("Failed to verify idToken")
            }
            //todo add facebook and VK
            else -> throw IllegalArgumentException("Unexpected provider: $provider")
        }
    }

    fun getAccessToken(email: String): OAuth2AccessToken? {

//        val authorizationParameters = HashMap<String, String>()
//        authorizationParameters["scope"] = "read"
//        authorizationParameters["username"] = "mobile_client"
//        authorizationParameters["client_id"] = "mobile-client"
//        authorizationParameters["grant"] = "password"
//
//        val authorizationRequest = OAuth2Request(authorizationParameters)
//        authorizationRequest.setApproved(true)
//
//        val authorities = HashSet<GrantedAuthority>()
//        authorities.add(SimpleGrantedAuthority("ROLE_UNTRUSTED_CLIENT"))
//        authorizationRequest.setAuthorities(authorities)
//
//        val resourceIds = HashSet<String>()
//        resourceIds.add("mobile-public")
//        authorizationRequest.setResourceIds(resourceIds)


        //this works
        val requestParameters = mapOf<String, String>()
        //todo move to constants
        val clientId = "client_id"
        //todo move to constants
        val authorities = setOf(SimpleGrantedAuthority("USER"))
        val approved = true
        //todo move to constants
        val scope = setOf("read write")
        val resourceIds = setOf<String>()
        val responseTypes = setOf("code")
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

        val authenticationToken = UsernamePasswordAuthenticationToken(
                usersService.loadUserByUsername(email),
                null,
                authorities
        )
        val auth = OAuth2Authentication(oAuth2Request, authenticationToken)
        println("${tokenServices.getAccessToken(auth)}")
        return tokenServices.createAccessToken(auth)
        //this works END


//        val authenticationToken = UsernamePasswordAuthenticationToken(usersService.loadUserByUsername(email), null, authorities)
//        val auth = oauth2authenticationManager.authenticate(authenticationToken.principal )
//        println("auth: $auth")
//        println("auth.principal: ${auth.principal}")
//        val auth2 = tokenServices.loadAuthentication(auth.principal as String)
//        return tokenServices.createAccessToken(auth2)
    }
}