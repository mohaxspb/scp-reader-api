package ru.kuchanov.scpreaderapi.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.provider.ClientDetails
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "oauth_client_details")
data class OAuthClientDetails(
        @Id
        val client_id: String,
        val resource_ids: String,
        val client_secret: String,
        val scope: String,
        val authorized_grant_types: String,
        val web_server_redirect_uri: String,
        val authorities: String,
        val access_token_validity: Int,
        val refresh_token_validity: Int,
        val additional_information: String,
        val autoapprove: String,
        @field:CreationTimestamp
        val created: Timestamp,
        @field:UpdateTimestamp
        val updated: Timestamp
) : ClientDetails {
    override fun isSecretRequired() = true

    override fun getAdditionalInformation(): MutableMap<String, Any> = mutableMapOf()

    override fun getAccessTokenValiditySeconds() = access_token_validity

    override fun getResourceIds(): MutableSet<String> = mutableSetOf()

    override fun getClientId() = client_id

    override fun isAutoApprove(scope: String?) = true

    override fun getAuthorities() = authorities
            .split(",")
            .map { SimpleGrantedAuthority(it) }

    override fun getRefreshTokenValiditySeconds() = refresh_token_validity

    override fun getClientSecret() = client_secret

    override fun getRegisteredRedirectUri(): MutableSet<String> = mutableSetOf()

    override fun isScoped() = true

    override fun getScope() = scope.split(",").toMutableSet()

    override fun getAuthorizedGrantTypes() = authorized_grant_types
            .split(",")
            .toMutableSet()
}

class ClientNotFoundError : RuntimeException()