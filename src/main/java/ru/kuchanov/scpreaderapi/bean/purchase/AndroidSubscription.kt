package ru.kuchanov.scpreaderapi.bean.purchase

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*

/**
 * [entity documentation][https://developers.google.com/android-publisher/api-ref/purchases/subscriptions#resource]
 */
@Entity
@Table(name = "android_subscriptions")
data class AndroidSubscription(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "auto_renewing")
        val autoRenewing: Boolean? = null,
        @Column(name = "expiry_time_millis")
        var expiryTimeMillis: Timestamp? = null,
        @Column(name = "start_time_millis")
        var startTimeMillis: Timestamp? = null,
        @Column(name = "user_cancellation_time_millis")
        var userCancellationTimeMillis: Timestamp? = null,
        @Column(name = "price_amount_micros")
        val priceAmountMicros: Long? = null,
        @Column(name = "price_currency_code")
        val priceCurrencyCode: String? = null,
        @Column(name = "order_id")
        var orderId: String,
        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null,
        //
        @Column(name = "android_package")
        val androidPackage: String,
        @Column(name = "purchase_token")
        val purchaseToken: String,
        /**
         * token of previously canceled subscription or one from witch this one was upgraded
         */
        @Column(name = "linked_purchase_token")
        var linkedPurchaseToken: String? = null
)