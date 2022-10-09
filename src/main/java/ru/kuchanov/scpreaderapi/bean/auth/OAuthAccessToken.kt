package ru.kuchanov.scpreaderapi.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.sql.Timestamp
import javax.persistence.*

@Deprecated("Use OAuthAuthorizationNew class and oauth_authorization_new table")
@Entity
@Table(name = "oauth_access_token")
@NoArgConstructor
data class OAuthAccessToken(
        @Id
        @Column(name = "token_id")
        val tokenId: String,
        val token: ByteArray,
        @Column(name = "authentication_id")
        val authenticationId: String,
        @Column(name = "user_name")
        val userName: String,
        @Column(name = "client_id")
        val clientId: String,
        val authentication: ByteArray,
        @Column(name = "refresh_token")
        val refreshToken: String,
        @field:CreationTimestamp
        val created: Timestamp,
        @field:UpdateTimestamp
        val updated: Timestamp
)

class OAuthAccessTokenNotFoundError : RuntimeException()