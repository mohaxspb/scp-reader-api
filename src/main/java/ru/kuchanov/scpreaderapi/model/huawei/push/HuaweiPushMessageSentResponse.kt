package ru.kuchanov.scpreaderapi.model.huawei.push

/**
 *
 * Documentation: https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/https-send-api-0000001050986197-V5#EN-US_TOPIC_0000001124288117__section26371025193915
 *
 * Example:
 * {
 * "code": "80000000",
 * "msg": "Success",
 * "requestId": "157440955549500001002006"
 * }
 */
data class HuaweiPushMessageSentResponse(
        val code: String,
        val msg: String,
        val requestId: String?
)