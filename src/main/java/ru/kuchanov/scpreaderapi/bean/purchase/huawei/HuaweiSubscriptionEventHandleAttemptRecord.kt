package ru.kuchanov.scpreaderapi.bean.purchase.huawei

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*


/**
 * [documentation][https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/api-notifications-about-subscription-events-0000001050706084-V5#EN-US_TOPIC_0000001050706084__section693011533393]
 */
@Entity
@Table(name = "huawei_subscription_event_handle_attempt")
data class HuaweiSubscriptionEventHandleAttemptRecord(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        //data
        @Column(name = "status_update_notification")
        val statusUpdateNotification: String,
        @Column(name = "notification_signature")
        val notificationSignature: String,

        @Column(name = "error_class", columnDefinition = "TEXT")
        var errorClass: String? = null,
        @Column(name = "error_message", columnDefinition = "TEXT")
        var errorMessage: String? = null,
        @Column(name = "stacktrace", columnDefinition = "TEXT")
        var stacktrace: String? = null,
        @Column(name = "cause_error_class", columnDefinition = "TEXT")
        var causeErrorClass: String? = null,
        @Column(name = "cause_error_message", columnDefinition = "TEXT")
        var causeErrorMessage: String? = null,
        @Column(name = "cause_stacktrace", columnDefinition = "TEXT")
        var causeStacktrace: String? = null,

        //dates
        @field:CreationTimestamp
        val created: LocalDateTime? = null,
        @field:UpdateTimestamp
        val updated: LocalDateTime? = null
)