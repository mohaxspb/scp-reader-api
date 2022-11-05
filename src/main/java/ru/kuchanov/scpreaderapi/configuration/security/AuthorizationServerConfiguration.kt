package ru.kuchanov.scpreaderapi.configuration.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.*
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
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
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandlerImpl
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.auth.AuthorityType
import ru.kuchanov.scpreaderapi.bean.auth.OAuthAccessToken
import ru.kuchanov.scpreaderapi.bean.users.User.Companion.USER_PROPERTY_NAME
import ru.kuchanov.scpreaderapi.service.auth.AccessTokenServiceImpl
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
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
//                println("clientCredentialsAuthProvider supports: $authentication")
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
    @Order(2)
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
    @Order(4)
    fun standardSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.cors().disable()

        http.csrf().disable()

        http.requestMatchers {
            it.requestMatchers(
                object : RequestMatcher {
                    override fun matches(request: HttpServletRequest): Boolean {
                        println("requestMatchers formLogin: ${request.requestURI}, ${request.getHeader(HttpHeaders.AUTHORIZATION)}")
                        return request.getHeader(HttpHeaders.AUTHORIZATION) == null
                    }
                }
            )
        }

        http
            .authorizeRequests()
            .antMatchers(*ignoredAntMatchers())
            .permitAll()

        http
            .authorizeRequests()
            .antMatchers(*adminAntMatchers())
            .hasAuthority(AuthorityType.ADMIN.name)

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

        http
            .authorizeRequests()
            .anyRequest()
            .authenticated()

        return http.build()
    }

    @Bean
    @Order(3)
    fun resourceServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors().disable()
            .csrf().disable()

            //check what request will be handled by this security filter chain
            //with AUTHORIZATION header in this case
            .requestMatchers {
                it.requestMatchers(
                    object : RequestMatcher {
                        override fun matches(request: HttpServletRequest): Boolean {
                            println(
                                "requestMatchers resource initial: ${request.requestURI}, ${
                                    request.getHeader(HttpHeaders.AUTHORIZATION)
                                }"
                            )
                            return request.getHeader(HttpHeaders.AUTHORIZATION) != null
                        }
                    }
                )
            }


            .authorizeRequests()
            .antMatchers(*adminAntMatchers())
            .hasAuthority(AuthorityType.ADMIN.name)
            .and()
            .authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()

            //this is used for authorizing with access token in request Authorization header
            .oauth2ResourceServer()
            //do not use default one, as it prevents requests without space between Bearer and token value
            .bearerTokenResolver { request ->
                val authorization: String? = request.getHeader(HttpHeaders.AUTHORIZATION)
//                println("BearerTokenResolver#resolve: ${request.requestURI}, authorization: $authorization")

                //ignore here by return null
                if (ignoredAntMatchers().map { AntPathRequestMatcher(it) }.any { it.matches(request) }) {
                    println("found ignored request - so return null from bearerTokenResolver")
                    return@bearerTokenResolver null
                }

                val token = authorization?.replaceFirst("Bearer", "", ignoreCase = true)?.trim()
                if (token.isNullOrEmpty()) {

                    //throw 401 error
                    throw InvalidBearerTokenException("Token is empty!")
                } else {
                    token
                }
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
//                            principal = principalFromAuth.clientId
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
            .and()
            .build()
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
                println("OLD tokenToFindInDb in MD5: $md5OfTokenToFindInDbWhichIsUsedAsTokenId")
                val oldAuthTokenInDb: OAuthAccessToken? =
                    accessTokenService.findFirstByTokenId(md5OfTokenToFindInDbWhichIsUsedAsTokenId)

                //switch between new and old access tokens
                if (oldAuthTokenInDb == null) {
                    println("OLD tokenToFindInDb is null!")

                    val newAuthTokenInDb = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN)
                        ?: kotlin.run {
                            println("PROVIDED token not found in DB (old or new tables). So throw error")
                            throw AuthenticationCredentialsNotFoundException("Token not found exception!")
                        }

                    println("NEW token expiration: ${newAuthTokenInDb.accessToken.isExpired}")
                    println("NEW token issuedAt: ${newAuthTokenInDb.accessToken.token.issuedAt}")
                    println("NEW token expiresAt: ${newAuthTokenInDb.accessToken.token.expiresAt}")
                    println("NEW token claims: ${newAuthTokenInDb.accessToken.claims}")

                    if (newAuthTokenInDb.accessToken.isExpired) {
                        println("PROVIDED token EXPIRED. So throw CredentialsExpiredException")
                        throw CredentialsExpiredException("Token expired exception!")
                    }

                    clientId = newAuthTokenInDb.registeredClientId
                    username = newAuthTokenInDb.principalName
                } else {
                    clientId = oldAuthTokenInDb.clientId
                    username = oldAuthTokenInDb.userName!!
                }

                val clientDetails = clientRegistrationRepository.findByRegistrationId(clientId)

                val isClientCredentialsAuth =
                    clientDetails.authorizationGrantType == AuthorizationGrantType.CLIENT_CREDENTIALS

                println("isClientCredentialsAuth: $isClientCredentialsAuth")

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

    /**
     * Provide custom tokenEndpoint and authorizationEndpoint
     * as old version of app uses this urls for token requests
     */
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

    @Bean
    fun ignoredAntMatchers() =
        arrayOf(
            "/",
            "/scp-reader/api/",
            "/encrypt",
            "/login**",
            "/error**",
            "/resources/**",
            "/image/**",
            "/${ScpReaderConstants.Path.AUTH}/**",
            "/${ScpReaderConstants.Path.PUSH}/${ScpReaderConstants.Path.MESSAGING}/all/byTypes",
            "/${ScpReaderConstants.Path.MONETIZATION}/${ScpReaderConstants.Path.PURCHASE}/subscriptionEvents/huawei",
            "/${ScpReaderConstants.Path.MONETIZATION}/${ScpReaderConstants.Path.PURCHASE}/subscriptionEvents/g_purchases",
            "/${ScpReaderConstants.Path.PURCHASE}/**",
            "/${ScpReaderConstants.Path.ADS}/all",
            "/${ScpReaderConstants.Path.ADS}/files/**"
        )

    @Bean
    fun adminAntMatchers() =
        arrayOf(
            "/${ScpReaderConstants.Path.FIREBASE}/**",
            "/${ScpReaderConstants.Path.ARTICLE}/${ScpReaderConstants.Path.PARSE}/**",
            "/${ScpReaderConstants.Path.ARTICLE}/**/delete",
            "/securedAdmin"
        )
}