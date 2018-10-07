package ru.kuchanov.scpreaderapi.bean.purchase

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
        val orderId: String? = null,
        @Column(name = "consumption_state")
        val consumptionState: Int? = null,
        @Column(name = "purchase_state")
        val purchaseState: Int? = null
)