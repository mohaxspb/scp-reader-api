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

        /**
         * Purchase status. The options are as follows:
         * -1: initialized
         * 0: purchased
         * 1: canceled
         * 2: refunded
         */
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
        /**
         * Same as [purchaseTime]
         */
        @Column(name = "start_time_millis")
        var startTimeMillis: Timestamp? = null,
        /**
         * Subscription cancellation time in UTC.
         * Note:cancelWay and cancellationTime are displayed when a subscription renewal stops (the refund is not involved).
         */
        @Column(name = "user_cancellation_time_millis")
        var userCancellationTimeMillis: Timestamp? = null,
        /**
         * Time when a subscription is revoked.
         * A refund is made and the service is unavailable immediately.
         * This parameter has a value when a user:
         * (1) makes a complaint and revokes a subscription through the customer service personnel;
         * (2) performs subscription upgrade or crossgrade that immediately takes effect and revokes the previous receipt of the original subscription.
         * Note: If a receipt is revoked, the purchase is not complete.
         */
        @Column(name = "cancel_time")
        val cancelTime: Timestamp? = null,
        @Column(name = "price_amount_micros")
        val priceAmountMicros: Long? = null,
        @Column(name = "price_currency_code")
        val priceCurrencyCode: String? = null,

        /**
         * Timestamp of the purchase time, which is
         * the number of milliseconds from 00:00:00 on January 1, 1970 to the purchase time.
         * If the purchase is not complete, this parameter is left empty.
         */
        @Column(name = "purchase_time")
        val purchaseTime: Timestamp? = null,

        @Column(name = "pay_order_id", columnDefinition = "TEXT")
        val payOrderId: String,
        @Column(name = "order_id")
        val orderId: String,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)