package ru.kuchanov.scpreaderapi.bean.purchase.google

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.LocalDateTime
import javax.persistence.*

/**
 * [entity documentation][https://developers.google.com/android-publisher/api-ref/purchases/subscriptions#resource]
 */
@Entity
@Table(name = "google_subscriptions")
data class GoogleSubscription(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "auto_renewing")
        val autoRenewing: Boolean? = null,
        @Column(name = "expiry_time_millis")
        val expiryTimeMillis: LocalDateTime? = null,
        @Column(name = "start_time_millis")
        val startTimeMillis: LocalDateTime? = null,
        @Column(name = "user_cancellation_time_millis")
        val userCancellationTimeMillis: LocalDateTime? = null,
        @Column(name = "price_amount_micros")
        val priceAmountMicros: Long? = null,
        @Column(name = "price_currency_code")
        val priceCurrencyCode: String? = null,
        @Column(name = "order_id")
        var orderId: String,
        //dates
        @field:CreationTimestamp
        val created: LocalDateTime? = null,
        @field:UpdateTimestamp
        val updated: LocalDateTime? = null,
        //
        @Column(name = "android_package")
        val androidPackage: String,
        @Column(name = "purchase_token")
        val purchaseToken: String,
        /**
         * token of previously canceled subscription or one from witch this one was upgraded
         */
        @Column(name = "linked_purchase_token")
        val linkedPurchaseToken: String? = null
)

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class GoogleSubscriptionNotFoundException(
        override val message: String? = "GoogleSubscription not found in db!"
) : RuntimeException(message)