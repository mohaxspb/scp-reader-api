package ru.kuchanov.scpreaderapi.bean.purchase.google

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*


/**
 * [documentation][]
 */
@Entity
@Table(name = "google_subscription_event_handle_attempt")
data class GoogleSubscriptionEventHandleAttemptRecord(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        //data
        @Column(name = "decoded_data_json")
        val decodedDataJson: String,
        @Column(name = "encoded_data")
        val encodedData: String,

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