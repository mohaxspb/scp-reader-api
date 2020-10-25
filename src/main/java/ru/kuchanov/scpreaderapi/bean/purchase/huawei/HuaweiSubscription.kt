package ru.kuchanov.scpreaderapi.bean.purchase.huawei

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*


/**
 * [entity documentation][https://developer.huawei.com/consumer/en/doc/development/HMS-References/iap-InAppPurchaseDetails-v4]
 */
@Entity
@Table(name = "huawei_subscriptions")
data class HuaweiSubscription(
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

        /**
         * This parameter uniquely identifies the mapping between a product and a user.
         * It does not change when the subscription is renewed
         */
        @Column(name = "subscription_id")
        val subscriptionId: String,
        /**
         * ID of the subscription group to which a subscription belongs.
         */
        @Column(name = "product_group")
        val productGroup: String,
        @Column(name = "sub_is_valid")
        val subIsValid: Boolean,
        @Column(name = "auto_renewing")
        val autoRenewing: Boolean,
        /**
         * can be filled if this subscription is switched from another
         */
        @Column(name = "ori_subscription_id")
        val oriSubscriptionId: String? = null,

        @Column(name = "account_flag", columnDefinition = "TEXT")
        val accountFlag: Int? = null,

        @Column(name = "purchase_state")
        val purchaseState: Int? = null,

        @Column(name = "android_package")
        val androidPackage: String,
        @Column(name = "purchase_token")
        val purchaseToken: String,
        /**
         * token of previously canceled subscription or one from witch this one was upgraded
         */
        @Column(name = "linked_purchase_token")
        var linkedPurchaseToken: String? = null,

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
        val updated: Timestamp? = null
)