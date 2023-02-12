package ru.kuchanov.scpreaderapi.repository.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.OAuth2TokenFormat
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.TokenSettings
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.auth.OAuthClientDetails
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant

@Service
class ScpRegisteredClientRepository @Autowired constructor(
    private val clientDetailsRepository: ClientDetailsRepository
) : RegisteredClientRepository {

    override fun save(registeredClient: RegisteredClient) {
        val oAuthClientDetails = OAuthClientDetails(
            client_id = registeredClient.clientId,
            resource_ids = "",
            client_secret = registeredClient.clientSecret!!,
            scope = registeredClient.scopes.joinToString(separator = ","),
            autoapprove = "true",
            access_token_validity = registeredClient.tokenSettings.accessTokenTimeToLive.seconds.toInt(),
            refresh_token_validity = registeredClient.tokenSettings.refreshTokenTimeToLive.seconds.toInt(),
            authorized_grant_types = registeredClient.authorizationGrantTypes.joinToString(
                separator = ",",
                transform = { it.value }),
            additional_information = "",
            authorities = registeredClient.scopes.joinToString(separator = ","),
            created = Timestamp.from(Instant.now()),
            updated = Timestamp.from(Instant.now()),
            web_server_redirect_uri = ""
        )

        clientDetailsRepository.save(oAuthClientDetails)
    }

    override fun findById(id: String): RegisteredClient? {
        val oAuthClientDetails = clientDetailsRepository.findByIdOrNull(id) ?: return null
        println("oAuthClientDetails: $oAuthClientDetails")
        return RegisteredClient
            .withId(oAuthClientDetails.client_id)
            .clientId(oAuthClientDetails.client_id)
            .authorizationGrantTypes {
                it.addAll(
                    oAuthClientDetails.authorized_grant_types
                        .split(",")
                        .map { AuthorizationGrantType(it) }
                )
            }
            .clientSecret(oAuthClientDetails.client_secret)
            .clientAuthenticationMethods {
                it.addAll(
                    setOf(
                        ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
                        ClientAuthenticationMethod.CLIENT_SECRET_POST,
                    )
                )
            }
            .scopes {
                it.addAll(oAuthClientDetails.authorities.split(","))
            }
            .tokenSettings(
                TokenSettings.builder()
                    .accessTokenTimeToLive(
                        Duration.ofSeconds(oAuthClientDetails.access_token_validity.toLong())
                    )
                    .refreshTokenTimeToLive(
                        Duration.ofDays(100_000)
                    )
                    .accessTokenFormat(OAuth2TokenFormat.REFERENCE)
                    .build()
            )
            .build()
    }

    override fun findByClientId(clientId: String): RegisteredClient? {
        println("findByClientId")
        return findById(clientId)
    }
}