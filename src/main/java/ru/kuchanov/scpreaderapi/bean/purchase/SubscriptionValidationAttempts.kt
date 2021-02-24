package ru.kuchanov.scpreaderapi.bean.purchase

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*


@Entity
@Table(name = "subscription_validate_attempts")
data class SubscriptionValidationAttempts(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @Column(name = "subscription_id")
        val subscriptionId: Long,
        @Column(columnDefinition = "TEXT")
        val store: String,

        var attempts: Int,
        @Column(name = "last_attempt_time")
        var lastAttemptTime: Timestamp,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
) : Serializable

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such SubscriptionValidationAttempts")
class SubscriptionValidationAttemptsNotFoundException : RuntimeException()