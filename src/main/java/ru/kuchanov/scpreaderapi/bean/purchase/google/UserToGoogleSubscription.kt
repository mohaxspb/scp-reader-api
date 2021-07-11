package ru.kuchanov.scpreaderapi.bean.purchase.google

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "user__to__google_subscriptions")
data class UserToGoogleSubscription(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "user_id")
    var userId: Long,
    @Column(name = "google_subscription_id")
    var googleSubscriptionId: Long,
    //dates
    @field:CreationTimestamp
    val created: LocalDateTime? = null,
    @field:UpdateTimestamp
    val updated: LocalDateTime? = null
) : Serializable