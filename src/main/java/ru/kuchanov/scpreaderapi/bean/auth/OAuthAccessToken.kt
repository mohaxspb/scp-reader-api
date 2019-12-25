package ru.kuchanov.scpreaderapi.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "oauth_access_token")
@NoArgConstructor
data class OAuthAccessToken(
        @Id
        val token_id: String,
        val token: ByteArray,
        val authentication_id: String,
        val user_name: String,
        val client_id: String,
        val authentication: ByteArray,
        val refresh_token: String,
        @field:CreationTimestamp
        val created: Timestamp,
        @field:UpdateTimestamp
        val updated: Timestamp
)
