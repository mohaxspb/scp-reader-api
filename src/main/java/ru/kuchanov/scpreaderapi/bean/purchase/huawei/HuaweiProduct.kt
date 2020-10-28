package ru.kuchanov.scpreaderapi.bean.purchase.huawei

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*

/**
 * [entity documentation][https://developer.huawei.com/consumer/en/doc/development/HMS-References/iap-InAppPurchaseDetails-v4]
 */
@Entity
@Table(name = "huawei_products")
data class HuaweiProduct(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        /**
         * 0: consumable
         * 1: non-consumable
         * 2: renewable subscription
         * 3: non-renewable subscription
         */
        val kind: Int,

        @Column(name = "product_id", columnDefinition = "TEXT")
        val productId: String? = null,
        @Column(name = "product_name", columnDefinition = "TEXT")
        val productName: String? = null,

        @Column(columnDefinition = "TEXT")
        val country: String? = null,

        /**
         * Value after the actual price of a product is multiplied by 100.
         * The actual price is accurate to two decimal places.
         * For example, if the value of this parameter is 501, the actual product price is 5.01.
         */
        val price: Long? = null,

        @Column(name = "account_flag", columnDefinition = "TEXT")
        val accountFlag: Int? = null,

        /**
         * Purchase status. The options are as follows:
         * -1: initialized
         * 0: purchased
         * 1: canceled
         * 2: refunded
         */
        @Column(name = "purchase_state")
        val purchaseState: Int? = null,
        /**
         * Consumption status, which exists only for one-off products. The options are as follows:
         * 0: not consumed
         * 1: consumed
         */
        @Column(name = "consumption_state")
        val consumptionState: Int,

        @Column(name = "purchase_time")
        val purchaseTime: Timestamp? = null,

        @Column(name = "android_package")
        val androidPackage: String,
        @Column(name = "purchase_token")
        val purchaseToken: String,

        @Column(name = "price_amount_micros")
        val priceAmountMicros: Long? = null,
        @Column(name = "price_currency_code")
        val priceCurrencyCode: String? = null,

        @Column(name = "order_id")
        val orderId: String,
        @Column(name = "pay_order_id", columnDefinition = "TEXT")
        val payOrderId: String,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)