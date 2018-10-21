package ru.kuchanov.scpreaderapi.bean.purchase

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*

/**
 * [entity documentation][https://developers.google.com/android-publisher/api-ref/purchases/products#resource]
 */
@Entity
@Table(name = "android_products")
data class AndroidProduct(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "purchase_time_millis")
        val purchaseTimeMillis: Timestamp? = null,
        @Column(name = "order_id")
        val orderId: String,
        /**
         * The consumption state of the inapp product. Possible values are:
         * 0 - Yet to be consumed
         * 1 - Consumed
         */
        @Column(name = "consumption_state")
        var consumptionState: Int? = null,
        /**
         * The purchase state of the order. Possible values are:
         * 0 - Purchased
         * 1 - Canceled
         */
        @Column(name = "purchase_state")
        var purchaseState: Int? = null,
        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        @Version
        val updated: Timestamp? = null,
        //
        @Column(name = "android_package")
        val androidPackage: String,
        @Column(name = "purchase_token")
        val purchaseToken: String,
        /**
         * The type of purchase of the inapp product. This field is only set if this purchase was not made using the standard in-app billing flow. Possible values are:
         * 0 - Test (i.e. purchased from a license testing account)
         * 1 - Promo (i.e. purchased using a promo code)
         */
        @Column(name = "purchase_type")
        val purchaseType: Int? = null
)