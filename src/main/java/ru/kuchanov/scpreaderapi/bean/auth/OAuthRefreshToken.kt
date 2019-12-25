package ru.kuchanov.scpreaderapi.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "oauth_refresh_token")
@NoArgConstructor
data class OAuthRefreshToken(
        @Id
        val token_id: String,
        val token: ByteArray,
        val authentication: ByteArray,
        @field:CreationTimestamp
        val created: Timestamp,
        @field:UpdateTimestamp
        @Column(insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
        val updated: Timestamp
)
