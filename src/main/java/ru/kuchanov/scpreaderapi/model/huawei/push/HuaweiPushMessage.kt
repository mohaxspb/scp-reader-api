package ru.kuchanov.scpreaderapi.model.huawei.push

import com.fasterxml.jackson.annotation.JsonProperty

data class HuaweiPushMessage(
        @JsonProperty("validate_only")
        val validateOnly: Boolean = false,
        val message: Message
) {
    data class Message(
            val android: Android = Android(),
            /**
             * in pseudo-JSON format, i.e. "{'type':'message', 'message':'test'}"
             */
            val data: String,
            val token: List<String>?,
            val topic: String?
    ) {
        init {
            check(token.isNullOrEmpty() && topic.isNullOrEmpty()) {
                "You must set topic or at least one token!"
            }
        }

        data class Android(
                val urgency: URGENCY = URGENCY.NORMAL,
                val ttl: String = TTL_DEFAULT
        ) {
            companion object {
                const val TTL_DEFAULT = "10000s"
            }

            enum class URGENCY {
                NORMAL
            }
        }
    }
}