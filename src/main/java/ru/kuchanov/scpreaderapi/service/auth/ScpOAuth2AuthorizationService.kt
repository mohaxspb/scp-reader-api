package ru.kuchanov.scpreaderapi.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.security.oauth2.core.OAuth2TokenType
import org.springframework.security.oauth2.core.OAuth2TokenType.ACCESS_TOKEN
import org.springframework.security.oauth2.core.OAuth2TokenType.REFRESH_TOKEN
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.auth.ClientNotFoundError
import ru.kuchanov.scpreaderapi.bean.auth.OAuthAuthorizationNew
import ru.kuchanov.scpreaderapi.bean.auth.OAuthAuthorizationNewNotFoundError
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.configuration.security.AuthorizationServerConfiguration.Companion.CLIENT_CREDENTIALS_TOKEN
import ru.kuchanov.scpreaderapi.repository.auth.OauthAuthorizationNewRepository
import ru.kuchanov.scpreaderapi.repository.auth.RefreshTokenRepository
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserService
import java.security.Principal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Service
class ScpOAuth2AuthorizationService @Autowired constructor(
    /**
     * Used as we need to search for old tokens
     */
    private val accessTokenService: AccessTokenServiceImpl,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val registeredClientRepository: RegisteredClientRepository,
    private val userDetailsService: ScpReaderUserService,
    private val oauthAuthorizationNewRepository: OauthAuthorizationNewRepository
) : OAuth2AuthorizationService {

    override fun save(authorization: OAuth2Authorization) {
        println("save authorizationGrantType: ${authorization.authorizationGrantType.value}")
        println("save principalName: ${authorization.principalName}")
        println("save accessToken value: ${authorization.accessToken.token.tokenValue}")
        println("save accessToken scopes: ${authorization.accessToken.token.scopes}")
        val refreshToken = authorization.refreshToken?.token
        println("save refreshToken: ${refreshToken?.tokenValue}")
        println("save refreshToken dates: ${refreshToken?.expiresAt}, ${refreshToken?.issuedAt}")
        val isClientCredentialsAuth = authorization.authorizationGrantType == AuthorizationGrantType.CLIENT_CREDENTIALS
        val authInDb = if (isClientCredentialsAuth) {
            oauthAuthorizationNewRepository.findFirstByRegisteredClientId(authorization.registeredClientId)
        } else {
            oauthAuthorizationNewRepository.findFirstByPrincipalName(authorization.principalName)
        }
        println("authInDb: $authInDb")

        val authToSave = OAuthAuthorizationNew(
            id = authInDb?.id,
            registeredClientId = authorization.registeredClientId,
            principalName = if (isClientCredentialsAuth) {
                null
            } else {
                authorization.principalName
            },
            authorizationGrantType = authorization.authorizationGrantType.value,
        )

        authToSave.accessTokenValue = authorization.accessToken.token.tokenValue
        authToSave.accessTokenScopes = authorization.accessToken.token.scopes.joinToString(separator = ",")
        authToSave.accessTokenIssuedAt =
            LocalDateTime.ofInstant(authorization.accessToken.token.issuedAt, ZoneOffset.UTC)
        authToSave.accessTokenExpiresAt =
            LocalDateTime.ofInstant(authorization.accessToken.token.expiresAt, ZoneOffset.UTC)

        if (authorization.refreshToken != null) {
            authToSave.refreshTokenValue = authorization.refreshToken?.token?.tokenValue
            authToSave.refreshTokenIssuedAt =
                LocalDateTime.ofInstant(authorization.refreshToken?.token?.issuedAt, ZoneOffset.UTC)
            authToSave.refreshTokenExpiresAt =
                LocalDateTime.ofInstant(authorization.refreshToken?.token?.expiresAt, ZoneOffset.UTC)
        }
        println("authToSave: $authToSave")
        oauthAuthorizationNewRepository.save(authToSave)
    }

    override fun remove(authorization: OAuth2Authorization) {
        println("remove: $authorization")
        oauthAuthorizationNewRepository.deleteById(authorization.id.toLong())
    }

    override fun findById(id: String): OAuth2Authorization? {
        println("findById: $id")
        throw NotImplementedError("Not yet implemented AND MUST NOT, AS IT'S USED ONLY IN UNUSED (IN OUR CASE) IMPL")
    }

    override fun findByToken(token: String, tokenType: OAuth2TokenType): OAuth2Authorization? {
        println("findByToken: $token, ${tokenType.value}")
        return when (tokenType) {
            CLIENT_CREDENTIALS_TOKEN -> {
                val registeredClient = registeredClientRepository.findById(token) ?: throw ClientNotFoundError()

                val newVersionAuth =
                    oauthAuthorizationNewRepository.findFirstByRegisteredClientId(registeredClient.clientId)
                        ?: throw OAuthAuthorizationNewNotFoundError()
                val accessTokenValue = newVersionAuth.accessTokenValue

                val accessTokenObject = OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    accessTokenValue,
                    Instant.now(),
                    Instant.now().plusMillis(registeredClient.tokenSettings.accessTokenTimeToLive.toMillis()),
                    registeredClient.scopes
                )

                OAuth2Authorization
                    .withRegisteredClient(registeredClient)
                    .id(newVersionAuth.id!!.toString())
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .principalName(registeredClient.clientId)
                    .accessToken(accessTokenObject)
                    .attribute(AUTHORIZED_SCOPE_ATTRIBUTE_NAME, registeredClient.scopes)
                    .attribute(
                        Principal::class.java.name,
                        UsernamePasswordAuthenticationToken(
                            registeredClient.clientId,
                            registeredClient.clientSecret,
                            registeredClient.scopes.map { SimpleGrantedAuthority(it) }
                        )
                    )
                    .build()
            }
            ACCESS_TOKEN -> {
                val accessTokenValue: String
                val refreshTokenValue: String?
                val registeredClient: RegisteredClient
                val principalName: String

                val isClientCredentialsToken: Boolean

                val newVersionAuth = oauthAuthorizationNewRepository.findFirstByAccessTokenValue(token)

                if (newVersionAuth != null) {
                    accessTokenValue = newVersionAuth.accessTokenValue!!
                    refreshTokenValue = newVersionAuth.refreshTokenValue
                    registeredClient = registeredClientRepository.findById(newVersionAuth.registeredClientId)
                        ?: throw ClientNotFoundError()
                    principalName = newVersionAuth.principalName ?: newVersionAuth.registeredClientId
                    isClientCredentialsToken =
                        registeredClient.authorizationGrantTypes.contains(AuthorizationGrantType.CLIENT_CREDENTIALS)
                } else {
                    println("New auth not found, try to find old auth")

//                    val accessToken = accessTokenService.findFirstByRefreshToken(token)
                    val accessToken = accessTokenService.findFirstByToken(token)
                        ?: throw NullPointerException("accessToken is null!")
                    println("accessToken: ${accessToken.clientId}, ${accessToken.userName}")
                    val accessTokenValueDeserialized = accessTokenService
                        .deserialize<DefaultOAuth2AccessToken>(accessToken.token)
                    println("accessTokenValueDeserialized: $accessTokenValueDeserialized")

//                    val refreshToken = refreshTokenRepository.findByIdOrNull(accessToken.refreshToken)
//                        ?: throw IllegalStateException("Refresh token not found!")
//                    val deserializedRefreshToken =
//                        accessTokenService.deserialize<DefaultOAuth2RefreshToken>(refreshToken.token)
//                    println("deserialized refreshToken: $deserializedRefreshToken")

                    accessTokenValue = if (accessTokenValueDeserialized.expiration.before(Date())) {
                        val keyGen = Base64StringKeyGenerator()
                        keyGen.generateKey()
                    } else {
                        accessTokenValueDeserialized.value
                    }
                    println("validAccessTokenValue: $accessTokenValue")
//                    refreshTokenValue = deserializedRefreshToken.value
                    refreshTokenValue = accessToken.refreshToken
                    registeredClient = registeredClientRepository.findById(accessToken.clientId)
                        ?: throw ClientNotFoundError()
                    principalName = accessToken.userName ?: registeredClient.clientId

                    isClientCredentialsToken =
                        registeredClient.authorizationGrantTypes.contains(AuthorizationGrantType.CLIENT_CREDENTIALS)
                }

                val scopes = registeredClient.scopes

                val accessTokenObject = OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    accessTokenValue,
                    Instant.now(),
                    Instant.now().plusMillis(registeredClient.tokenSettings.accessTokenTimeToLive.toMillis()),
                    scopes
                )
                val now = Instant.now()
                val refreshTokenObject: OAuth2RefreshToken? = refreshTokenValue?.let {
                    OAuth2RefreshToken(
                        refreshTokenValue,
                        now,
                        now.plusMillis(registeredClient.tokenSettings.refreshTokenTimeToLive.toMillis())
                    )
                }

                val authorities: Collection<GrantedAuthority>

                val (username, password) = if (isClientCredentialsToken) {
                    authorities = scopes.map { SimpleGrantedAuthority(it) }
                    registeredClient.clientId to registeredClient.clientSecret
                } else {
                    val user = userDetailsService.loadUserByUsername(principalName)
                    authorities = user.authorities
                    user.username to user.password
                }

                var builder = OAuth2Authorization
                    .withRegisteredClient(registeredClient)
                    .id(newVersionAuth?.id?.toString())
                    .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                    .principalName(principalName)
                    .accessToken(accessTokenObject)
                    .attribute(AUTHORIZED_SCOPE_ATTRIBUTE_NAME, scopes)
                    .attribute(
                        Principal::class.java.name,
                        UsernamePasswordAuthenticationToken(
                            username,
                            password,
                            authorities
                        )
                    )
                if (refreshTokenValue != null) {
                    builder = builder.refreshToken(refreshTokenObject)
                }

                builder.build()
            }
            REFRESH_TOKEN -> {
                val accessTokenValue: String
                val refreshTokenValue: String
                val registeredClient: RegisteredClient
                val username: String

                //try to find token in new table.
                val newVersionAuth = oauthAuthorizationNewRepository.findFirstByRefreshTokenValue(token)

                if (newVersionAuth == null) {
                    println("New auth not found, try to find old auth")
                    val refreshToken = refreshTokenRepository.findByIdOrNull(accessTokenService.extractTokenKey(token))
                        ?: throw IllegalStateException("Refresh token not found!")
                    val deserializedRefreshToken =
                        accessTokenService.deserialize<DefaultOAuth2RefreshToken>(refreshToken.token)
                    println("deserialized refreshToken: $deserializedRefreshToken")

                    val accessToken = accessTokenService.findFirstByRefreshToken(refreshToken.token_id)
                        ?: throw NullPointerException("accessToken is null!")
                    println("accessToken: ${accessToken.clientId}, ${accessToken.userName}")
                    val accessTokenValueDeserialized = accessTokenService
                        .deserialize<DefaultOAuth2AccessToken>(accessToken.token)
                    println("accessTokenValueDeserialized: $accessTokenValueDeserialized")

                    accessTokenValue = if (accessTokenValueDeserialized.expiration.before(Date())) {
                        val keyGen = Base64StringKeyGenerator()
                        keyGen.generateKey()
                    } else {
                        accessTokenValueDeserialized.value
                    }
                    println("validAccessTokenValue: $accessTokenValue")
                    refreshTokenValue = deserializedRefreshToken.value
                    registeredClient = registeredClientRepository.findById(accessToken.clientId)
                        ?: throw ClientNotFoundError()
                    username = accessToken.userName!!
                } else {
                    println("New auth found")
                    accessTokenValue = newVersionAuth.accessTokenValue!!
                    refreshTokenValue = newVersionAuth.refreshTokenValue!!
                    registeredClient = registeredClientRepository.findById(newVersionAuth.registeredClientId)
                        ?: throw ClientNotFoundError()
                    username = newVersionAuth.principalName!!
                }

                val scopes = registeredClient.scopes
                println("scopes: $scopes")

                val accessTokenObject = OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    accessTokenValue,
                    Instant.now(),
                    Instant.now().plusMillis(registeredClient.tokenSettings.accessTokenTimeToLive.toMillis()),
                    scopes
                )
                val now = Instant.now()
                val refreshTokenObject = OAuth2RefreshToken(
                    refreshTokenValue,
                    now,
                    now.plusMillis(registeredClient.tokenSettings.refreshTokenTimeToLive.toMillis())
                )

                val user = userDetailsService.loadUserByUsername(username)
                    ?: throw UserNotFoundException()

                OAuth2Authorization
                    .withRegisteredClient(registeredClient)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .principalName(user.username)
                    .accessToken(accessTokenObject)
                    .refreshToken(refreshTokenObject)
                    .attribute(AUTHORIZED_SCOPE_ATTRIBUTE_NAME, scopes)
                    .attribute(
                        Principal::class.java.name,
                        UsernamePasswordAuthenticationToken(
                            user.username,
                            user.password,
                            user.authorities
                        )
                    )
                    .build()
            }
            else -> throw IllegalArgumentException("Unexpected token type: ${tokenType.value}")
        }
    }
}