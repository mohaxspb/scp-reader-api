package ru.kuchanov.scpreaderapi.bean.purchase

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
        val expiryTimeMillis: Timestamp? = null,
        @Column(name = "start_time_millis")
        val startTimeMillis: Timestamp? = null,
        @Column(name = "user_cancellation_time_millis")
        val userCancellationTimeMillis: Timestamp? = null,
        @Column(name = "price_amount_micros")
        val priceAmountMicros: Long? = null,
        @Column(name = "price_currency_code")
        val priceCurrencyCode: String? = null,
        @Column(name = "order_id")
        val orderId: String? = null

        //todo add dates
        //add tokens list
        //add user id
)