package ru.kuchanov.scpreaderapi.bean.auth.huawei

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "huawei__oauth_access_token")
@NoArgConstructor
data class HuaweiOAuthAccessToken(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @Column(name = "client_id")
        val clientId: String,

        @Column(name = "access_token")
        val accessToken: String,
        /**
         * i.e. 3600
         */
        @Column(name = "expires_in")
        val expiresIn: Int,
        /**
         * i.e. `Bearer`
         */
        @Column(name = "token_type")
        val tokenType: String,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)
