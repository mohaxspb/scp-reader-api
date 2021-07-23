package ru.kuchanov.scpreaderapi.model.monetization.google.subscription.subevent

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonValue
import ru.kuchanov.scpreaderapi.model.monetization.google.subscription.subevent.DeveloperNotification.SubscriptionNotification.NotificationType

/**
 * [documentation](https://developer.android.com/google/play/billing/rtdn-reference#sub)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleSubscriptionEventDto(
    val message: Message,
    /**
     * i.e. "projects/myproject/subscriptions/mysubscription"
     */
    val subscription: String
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Message(
        val attributes: Map<String, String>,
        /**
         * base64-encoded data field, with DeveloperNotification inside it
         */
        val data: String,
        val publishTime: String,
        val messageId: String,
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeveloperNotification(
    /**
     * The version of this notification. Initially, this is "1.0". This version is distinct from other version fields.
     */
    val version: String,
    /**
     * The package name of the application that this notification relates to (for example, `com.some.thing`).
     */
    val packageName: String,
    /**
     * The timestamp when the event occurred, in milliseconds since the Epoch.
     */
    val eventTimeMillis: Long,
    /**
     * If this field is present, then this notification is related to a subscription,
     * and this field contains additional information related to the subscription.
     * Note that this field is mutually exclusive with testNotification and oneTimeProductNotification.
     */
    val subscriptionNotification: SubscriptionNotification?
) {
    data class SubscriptionNotification(
        /**
         * The version of this notification. Initially, this is "1.0". This version is distinct from other version fields.
         */
        val version: String,
        /**
         * see [NotificationType]
         */
        val notificationType: NotificationType,
        /**
         * The token provided to the user's device when the subscription was purchased.
         */
        val purchaseToken: String,
        /**
         * The purchased subscription ID (for example, "monthly001").
         */
        val subscriptionId: String,
    ) {
        enum class NotificationType(val value: Int) {
            /**
             * A subscription was recovered from account hold.
             */
            SUBSCRIPTION_RECOVERED(1),

            /**
             * An active subscription was renewed.
             */
            SUBSCRIPTION_RENEWED(2),

            /**
             * A subscription was either voluntarily or involuntarily cancelled. For voluntary cancellation, sent when the user cancels.
             */
            SUBSCRIPTION_CANCELED(3),

            /**
             * A new subscription was purchased.
             */
            SUBSCRIPTION_PURCHASED(4),

            /**
             * A subscription has entered account hold (if enabled).
             */
            SUBSCRIPTION_ON_HOLD(5),

            /**
             * A subscription has entered grace period (if enabled).
             */
            SUBSCRIPTION_IN_GRACE_PERIOD(6),

            /**
             * User has reactivated their subscription from Play > Account > Subscriptions (requires opt-in for subscription restoration).
             */
            SUBSCRIPTION_RESTARTED(7),

            /**
             * A subscription price change has successfully been confirmed by the user.
             */
            SUBSCRIPTION_PRICE_CHANGE_CONFIRMED(8),

            /**
             * A subscription's recurrence time has been extended.
             */
            SUBSCRIPTION_DEFERRED(9),

            /**
             * A subscription has been paused.
             */
            SUBSCRIPTION_PAUSED(10),

            /**
             * A subscription pause schedule has been changed.
             */
            SUBSCRIPTION_PAUSE_SCHEDULE_CHANGED(11),

            /**
             * A subscription has been revoked from the user before the expiration time.
             */
            SUBSCRIPTION_REVOKED(12),

            /**
             * A subscription has expired.
             */
            SUBSCRIPTION_EXPIRED(13);

            @JsonValue
            fun serializedValue() = value
        }
    }
}