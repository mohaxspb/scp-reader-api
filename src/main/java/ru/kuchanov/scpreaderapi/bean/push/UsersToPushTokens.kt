package ru.kuchanov.scpreaderapi.bean.push

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "users_to_push_tokens")
data class UsersToPushTokens(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @Column(name = "user_id")
        val userId: Long,
        @Column(name = "push_token_value")
        val pushTokenValue: String,
        @Enumerated(EnumType.STRING)
        @Column(name = "push_token_provider", columnDefinition = "TEXT")
        val pushTokenProvider: ScpReaderConstants.Push.Provider,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)

@ResponseStatus(value = HttpStatus.CONFLICT)
class UsersToPushTokensAlreadyExistsException(
        override val message: String? = "This token already in DB!"
) : RuntimeException(message)