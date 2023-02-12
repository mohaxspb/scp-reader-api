package ru.kuchanov.scpreaderapi.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.auth.ClientNotFoundError
import ru.kuchanov.scpreaderapi.repository.auth.ClientDetailsRepository


@Service
class ClientServiceImpl @Autowired constructor(
    val repository: ClientDetailsRepository
) : ClientRegistrationRepository {

    override fun findByRegistrationId(registrationId: String?): ClientRegistration {
        println("findByRegistrationId: $registrationId")
        return repository.findByIdOrNull(registrationId)?.let {
            println("findByRegistrationId authorities: ${it.authorities}")
            ClientRegistration
                .withRegistrationId(it.client_id)
                .clientId(it.client_id)
                .scope(it.authorities.split(","))
                .clientSecret(it.client_secret)
                .authorizationGrantType(
                    AuthorizationGrantType(it.authorized_grant_types.split(",").first())
                )
                .tokenUri(it.web_server_redirect_uri.ifEmpty { "just something to pass validation in fucking spring" })
                .build()
        } ?: throw ClientNotFoundError()
    }
}
