package ru.kuchanov.scpreaderapi.model.monetization.google.subscription.subevent

/**
 * [documentation](https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/api-notifications-about-subscription-events-0000001050706084-V5#EN-US_TOPIC_0000001050706084__table15297548123520)
 */
data class GoogleSubscriptionEventResponse(
        val errorCode: String = "0",
        val errorMsg: String = "success"
)