package ru.kuchanov.scpreaderapi.configuration.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.*
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.authentication.*
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandlerImpl
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import ru.kuchanov.scpreaderapi.bean.auth.AuthorityType
import ru.kuchanov.scpreaderapi.bean.auth.OAuthAccessToken
import ru.kuchanov.scpreaderapi.bean.auth.OAuthAccessTokenNotFoundError
import ru.kuchanov.scpreaderapi.bean.users.User.Companion.USER_PROPERTY_NAME
import ru.kuchanov.scpreaderapi.service.auth.AccessTokenServiceImpl
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Configuration
class AuthorizationServerConfiguration @Autowired constructor(
    private val accessTokenService: AccessTokenServiceImpl,
    private val userDetailsService: UserDetailsService,
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val authorizationService: OAuth2AuthorizationService,
) {

    companion object {
        val CLIENT_CREDENTIALS_TOKEN = OAuth2TokenType("CLIENT_CREDENTIALS_TOKEN")
    }

    @Bean
    fun refreshTokenAuthProvider(): AuthenticationProvider {
        val provider = OAuth2RefreshTokenAuthenticationProvider(authorizationService, tokenGenerator())

        return object : AuthenticationProvider {
            override fun authenticate(authentication: Authentication?): Authentication {
                println("refreshTokenAuthProvider authenticate: $authentication")
                return provider.authenticate(authentication)
            }

            override fun supports(authentication: Class<*>): Boolean {
                println("refreshTokenAuthProvider supports: $authentication")
                return OAuth2RefreshTokenAuthenticationToken::class.java.isAssignableFrom(authentication)
            }
        }
    }

    @Bean
    fun clientCredentialsAuthProvider(): AuthenticationProvider {
        val provider = OAuth2ClientCredentialsAuthenticationProvider(
            authorizationService,
            tokenGenerator()
        )

        return object : AuthenticationProvider {
            override fun authenticate(authentication: Authentication?): Authentication {
                println("clientCredentialsAuthProvider authenticate: $authentication")

                //do not use provider if token already exists
                val clientPrincipal = authentication?.principal as? OAuth2ClientAuthenticationToken
                val registeredClient = clientPrincipal?.registeredClient
                val accessToken =
                    authorizationService.findByToken(registeredClient?.clientId, CLIENT_CREDENTIALS_TOKEN)

                return if (accessToken != null) {
                    OAuth2AccessTokenAuthenticationToken(
                        registeredClient,
                        clientPrincipal,
                        accessToken.accessToken.token
                    )
                } else {
                    provider.authenticate(authentication)
                }
            }

            override fun supports(authentication: Class<*>): Boolean {
                println("clientCredentialsAuthProvider supports: $authentication")
                return OAuth2ClientCredentialsAuthenticationToken::class.java.isAssignableFrom(authentication)
            }
        }
    }

    @Bean
    fun providerManager(): ProviderManager {
        return object : ProviderManager(
            refreshTokenAuthProvider(),
            clientCredentialsAuthProvider()
        ) {
            override fun authenticate(authentication: Authentication?): Authentication {
                println("providerManager authenticate: $authentication")
                providers.forEach {
                    if (it.supports(authentication!!::class.java)) {
                        return it.authenticate(authentication)
                    }
                }

                return super.authenticate(authentication)
            }
        }
    }

    @Bean
    @Order(1)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)

        http
            .authenticationProvider(object : AuthenticationProvider {
                override fun authenticate(authentication: Authentication?): Authentication {
                    println("authenticationProvider authenticate: $authentication")
                    return providerManager().authenticate(authentication)
                }

                override fun supports(authentication: Class<*>?): Boolean {
                    return true
                }
            })

        return http.build()
    }

    @Bean
    @Order(2)
    fun standardSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.cors().disable()

        http.csrf().disable()

        http
            .authorizeRequests()
            .antMatchers(
                "/",
                "/auth/**",
            )
            .permitAll()
        http
            .authorizeRequests()
            .antMatchers("/securedAdmin")
            .hasAuthority(AuthorityType.ADMIN.name)
        http
            .authorizeRequests()
            .anyRequest()
            .authenticated()
        //this is default login page and authorization with cookies
        http
            .formLogin()
            .successHandler(object : SavedRequestAwareAuthenticationSuccessHandler() {
                override fun handle(
                    request: HttpServletRequest,
                    response: HttpServletResponse,
                    authentication: Authentication
                ) {
                    println("request: $request")
                    println("response: $response")
                    println("authentication: ${authentication.details}")
                    println("authentication: ${authentication.principal}")
                    super.handle(request, response, authentication)
                }
            })
            .permitAll()

        //this is used for authorizing with access token in request Authorization header
        http
            .oauth2ResourceServer()
            //do not use default one, as it prevents requests without space between Bearer and token value
            .bearerTokenResolver { request ->
                val authorization: String? = request.getHeader(HttpHeaders.AUTHORIZATION)

                authorization?.replaceFirst("Bearer", "")?.trim()
            }
            .accessDeniedHandler(object : AccessDeniedHandlerImpl() {
                override fun handle(
                    request: HttpServletRequest?,
                    response: HttpServletResponse?,
                    accessDeniedException: AccessDeniedException?
                ) {
                    println("accessDeniedHandler: $response")
                    println("accessDeniedHandler: $accessDeniedException")
                    super.handle(request, response, accessDeniedException)
                }
            })
            .opaqueToken { configurer ->
                configurer
                    .introspector(opaqueTokenIntrospector())
                    .authenticationManager {
                        println("opaqueToken authenticationManager: ${it.principal}, ${it.name}, ${it.details}, ${it.credentials}")
                        val auth: OAuth2AuthenticatedPrincipal =
                            opaqueTokenIntrospector().introspect(it.principal as String)
                        val oAuth2AuthenticatedPrincipal = auth as DefaultOAuth2AuthenticatedPrincipal

                        //handle client credentials token (no user)
                        val principalFromAuth = oAuth2AuthenticatedPrincipal.attributes[USER_PROPERTY_NAME]!!

                        val principal: Any?
                        val password: String
                        val authorities: Collection<GrantedAuthority>
                        if (principalFromAuth is UserDetails) {
                            principal = principalFromAuth
                            password = principalFromAuth.password
                            authorities = principalFromAuth.authorities
                        } else if (principalFromAuth is ClientRegistration) {
                            principal = null
                            password = principalFromAuth.clientSecret
                            authorities = principalFromAuth.scopes.map { SimpleGrantedAuthority(it) }
                        } else {
                            throw IllegalArgumentException("unexpected principal type: ${principalFromAuth::class.java.simpleName}")
                        }

                        UsernamePasswordAuthenticationToken(
                            principal,
                            password,
                            authorities
                        )
                    }
            }

        return http.build()
    }

    @Bean
    fun opaqueTokenIntrospector(): OpaqueTokenIntrospector {
        return object : OpaqueTokenIntrospector {
            override fun introspect(token: String?): OAuth2AuthenticatedPrincipal {
                if (token == null) {
                    throw OAuth2IntrospectionException("Token is null!")
                }
                //get user details by finding username from access token value
//                val myToken = accessTokenService.findFirstByUserName("mohax.spb@gmail.com")!!
//                println("myToken: ${myToken.tokenId}")
//                val test = myToken.token.decodeToString()
//                println("myToken: $test")
//                val test1 = accessTokenService.deserialize<DefaultOAuth2AccessToken>(myToken.token)
//                println("myToken: $test1")

                val clientId: String
                val username: String

                println("token from request: $token")

                val md5OfTokenToFindInDbWhichIsUsedAsTokenId = accessTokenService.extractTokenKey(token)!!
                println("tokenToFindInDb: $md5OfTokenToFindInDbWhichIsUsedAsTokenId")
                val oldAuthTokenInDb: OAuthAccessToken? =
                    accessTokenService.findFirstByTokenId(md5OfTokenToFindInDbWhichIsUsedAsTokenId)

                //switch between new and old access tokens
                if (oldAuthTokenInDb == null) {
                    val newAuthTokenInDb = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN)
                        ?: throw OAuthAccessTokenNotFoundError()
                    clientId = newAuthTokenInDb.registeredClientId
                    username = newAuthTokenInDb.principalName
                } else {
                    clientId = oldAuthTokenInDb.clientId
                    username = oldAuthTokenInDb.userName!!
                }

                val clientDetails = clientRegistrationRepository.findByRegistrationId(clientId)

                val isClientCredentialsAuth =
                    clientDetails.authorizationGrantType == AuthorizationGrantType.CLIENT_CREDENTIALS

                val attributes = mutableMapOf<String, Any>()
                val authorities = mutableListOf<GrantedAuthority>()

                if (isClientCredentialsAuth) {
                    attributes[USER_PROPERTY_NAME] = clientDetails
                    authorities += SimpleGrantedAuthority(AuthorityType.USER.name)
                } else {
                    val user = userDetailsService.loadUserByUsername(username)
                    println("user: $user")
                    println("user: ${user.authorities.map { it.authority }}")
                    attributes[USER_PROPERTY_NAME] = user
                    authorities += user.authorities
                }

                return DefaultOAuth2AuthenticatedPrincipal(attributes, authorities)
            }
        }
    }

    @Bean
    fun providerSettings(): ProviderSettings {
        return ProviderSettings.builder()
            .tokenEndpoint("/oauth/token")
            .authorizationEndpoint("/oauth/authorize")
            .build()
    }

    @Bean
    fun tokenGenerator(): OAuth2TokenGenerator<out OAuth2Token> {
        return OAuth2AccessTokenGenerator()
    }
}