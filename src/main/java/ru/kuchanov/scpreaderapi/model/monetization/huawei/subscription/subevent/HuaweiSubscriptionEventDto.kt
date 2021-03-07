package ru.kuchanov.scpreaderapi.model.monetization.huawei.subscription.subevent

import com.fasterxml.jackson.annotation.JsonValue

/**
 * [documentation](https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/api-notifications-about-subscription-events-0000001050706084-V5#EN-US_TOPIC_0000001050706084__table15297548123520)
 */
data class HuaweiSubscriptionEventDto(
        val statusUpdateNotification: String,
        @Suppress("SpellCheckingInspection")
        val notifycationSignature: String
)