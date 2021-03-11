package ru.kuchanov.scpreaderapi.model.monetization.huawei

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class InAppPurchaseData(
        val applicationId: Long,
        val autoRenewing: Boolean,
        val orderId: String?,
        /**
         * 0: consumable
         * 1: non-consumable
         * 2: renewable subscription
         * 3: non-renewable subscription
         */
        val kind: Int,
        val packageName: String?,
        val productId: String,
        val productName: String?,
        val purchaseState: Int,
        val developerPayload: String?,
        val developerChallenge: String?,
        val consumptionState: Int?,
        val purchaseToken: String,
        val purchaseType: Int?,
        val currency: String?,
        val price: Long?,
        val country: String?,
        val payType: String?,
        val payOrderId: String?,
        val accountFlag: Int?,
        //The following parameters are returned only in the subscription scenario.
        /**
         * Mandatory for subs
         */
        val lastOrderId: String?,
        val productGroup: String?,
        val purchaseTime: Long?,
        val oriPurchaseTime: Long?,
        val subscriptionId: String?,
        val oriSubscriptionId: String?,
        val quantity: Int?,
        val daysLasted: Long?,
        val numOfPeriods: Long?,
        val numOfDiscount: Long?,
        val expirationDate: Long?,
        val expirationIntent: Int?,
        val retryFlag: Int?,
        val introductoryFlag: Int?,
        val trialFlag: Int?,
        val cancelTime: Long?,
        val cancelReason: Int?,
        val appInfo: String?,
        val notifyClosed: Int?,
        val renewStatus: Int?,
        val priceConsentStatus: Int?,
        val renewPrice: Long?,
        val subIsvalid: Boolean?,
        val deferFlag: Int?,
        val cancelWay: Int?,
        val cancellationTime: Long?,
        val cancelledSubKeepDays: Int?,
        val confirmed: Int?,
        val resumeTime: Long?,
        val surveyReason: Int?,
        val surveyDetails: String?
)

enum class HuaweiProductKind constructor(type: Int) {
    CONSUMABLE(0),
    NON_CONSUMABLE(1),
    RENEWABLE_SUBSCRIPTION(2),
    NON_RENEWABLE_SUBSCRIPTION(3);

    companion object {
        fun findByType(type: Int): HuaweiProductKind =
                when (type) {
                    0 -> CONSUMABLE
                    1 -> NON_CONSUMABLE
                    2 -> RENEWABLE_SUBSCRIPTION
                    3 -> NON_RENEWABLE_SUBSCRIPTION
                    else -> throw IllegalArgumentException("Unexpected kind: $type")
                }
    }
}