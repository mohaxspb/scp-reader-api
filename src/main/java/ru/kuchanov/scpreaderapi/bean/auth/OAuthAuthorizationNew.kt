package ru.kuchanov.scpreaderapi.bean.auth

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.LocalDateTime
import javax.persistence.*


@Entity
@Table(name = "oauth_authorization_new")
data class OAuthAuthorizationNew(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "registered_client_id")
    val registeredClientId: String,
    @Column(name = "principal_name")
    val principalName: String?,
    @Column(name = "authorization_grant_type")
    val authorizationGrantType: String,

    @Column(name = "access_token_value")
    var accessTokenValue: String? = null,
    @Column(name = "access_token_issued_at")
    var accessTokenIssuedAt: LocalDateTime? = null,
    @Column(name = "access_token_expires_at")
    var accessTokenExpiresAt: LocalDateTime? = null,
    @Column(name = "access_token_scopes")
    var accessTokenScopes: String? = null,

    @Column(name = "refresh_token_value")
    var refreshTokenValue: String? = null,
    @Column(name = "refresh_token_issued_at")
    var refreshTokenIssuedAt: LocalDateTime? = null,
    @Column(name = "refresh_token_expires_at")
    var refreshTokenExpiresAt: LocalDateTime? = null,
)

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class OAuthAuthorizationNewNotFoundError : RuntimeException()