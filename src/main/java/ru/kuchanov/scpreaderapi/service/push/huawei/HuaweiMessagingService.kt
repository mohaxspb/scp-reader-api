package ru.kuchanov.scpreaderapi.service.push.huawei

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.Application
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessageNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.configuration.HuaweiApiConfiguration.Companion.HUAWEI_PUSH_SENDING_PARTIAL_SUCCESS_CODE
import ru.kuchanov.scpreaderapi.configuration.HuaweiApiConfiguration.Companion.HUAWEI_PUSH_SENDING_SUCCESS_CODE
import ru.kuchanov.scpreaderapi.model.exception.ScpServerException
import ru.kuchanov.scpreaderapi.model.huawei.push.HuaweiPushMessage
import ru.kuchanov.scpreaderapi.network.HuaweiPushApi
import ru.kuchanov.scpreaderapi.service.push.PushMessageService
import ru.kuchanov.scpreaderapi.service.push.PushProviderMessagingService
import ru.kuchanov.scpreaderapi.service.push.UserToPushTokensService
import java.net.HttpURLConnection

@Service
class HuaweiMessagingService @Autowired constructor(
        @Value("\${my.api.huawei.client_id}") private val huaweiClientId: String,
        private val pushMessageService: PushMessageService,
        private val userToPushTokensService: UserToPushTokensService,
        private val huaweiPushApi: HuaweiPushApi,
        private val objectMapper: ObjectMapper,
        @Qualifier(Application.HUAWEI_LOGGER) private val log: Logger
) : PushProviderMessagingService {

    override fun sendMessageToUserById(userId: Long, pushMessageId: Long, author: User): PushMessage {
        checkNotNull(author.id)
        val savedMessage = pushMessageService.findOneById(pushMessageId) ?: throw PushMessageNotFoundException()

        val userHuaweiTokens = userToPushTokensService.findByUserIdAndPushTokenProvider(
                userId,
                ScpReaderConstants.Push.Provider.HUAWEI
        )
        log.error("sendMessageToUserById: $userHuaweiTokens")

        return sendMessage(
                userPushTokens = userHuaweiTokens.map { it.pushTokenValue },
                pushMessage = savedMessage
        )
    }

    override fun sendMessageToUser(
            userId: Long,
            type: ScpReaderConstants.Push.MessageType,
            title: String,
            message: String,
            author: User
    ): PushMessage {
        checkNotNull(author.id)
        val savedMessage = pushMessageService.save(
                PushMessage(
                        userId = userId,
                        type = type,
                        title = title,
                        message = message,
                        authorId = author.id
                )
        )

        val userHuaweiTokens = userToPushTokensService.findByUserIdAndPushTokenProvider(
                userId,
                ScpReaderConstants.Push.Provider.HUAWEI
        )

        return sendMessage(
                userPushTokens = userHuaweiTokens.map { it.pushTokenValue },
                pushMessage = savedMessage
        )
    }

    override fun sendMessageToTopic(
            topicName: String,
            type: ScpReaderConstants.Push.MessageType,
            title: String,
            message: String,
            url: String?,
            author: User
    ): PushMessage {
        val savedMessage = pushMessageService.save(
                PushMessage(
                        topicName = topicName,
                        type = type,
                        title = title,
                        message = message,
                        url = url,
                        authorId = author.id!!
                )
        )

        return sendMessage(topicName = topicName, pushMessage = savedMessage)
    }

    override fun sendMessageToTopicById(
            topicName: String,
            pushMessageId: Long
    ): PushMessage {
        val savedMessage = pushMessageService.findOneById(pushMessageId) ?: throw PushMessageNotFoundException()

        return sendMessage(topicName = topicName, pushMessage = savedMessage)
    }

    private fun sendMessage(
            topicName: String? = null,
            userPushTokens: List<String>? = null,
            pushMessage: PushMessage
    ): PushMessage {
        val data = createDataString(pushMessageToMap(pushMessage))

        try {
            val result = huaweiPushApi.send(
                    applicationId = huaweiClientId,
                    huaweiPushMessage = HuaweiPushMessage(
                            message = HuaweiPushMessage.Message(
                                    data = data,
                                    topic = topicName,
                                    token = userPushTokens
                            )
                    )
            ).execute()
            if (result.isSuccessful && result.code() == HttpURLConnection.HTTP_OK) {
                //check huawei response
                val response = result.body()
                        ?: throw ScpServerException(
                                "Body is null while send push to topic to Huawei",
                                NullPointerException()
                        )
                when (response.code) {
                    HUAWEI_PUSH_SENDING_SUCCESS_CODE -> {
                        return pushMessageService.save(pushMessage.apply { sent = true })
                    }
                    HUAWEI_PUSH_SENDING_PARTIAL_SUCCESS_CODE -> {
                        try {
                            val responseDetails = objectMapper.readValue(
                                    response.msg,
                                    PushSendingPartialSuccess::class.java
                            )
                            log.error("Handle partial push sending success: " +
                                    "${responseDetails.success}/${responseDetails.success}: " +
                                    "${responseDetails.illegalTokens}")
                            responseDetails.illegalTokens.forEach {
                                userToPushTokensService.deleteByPushTokenValue(it)
                            }
                        } catch (e: Exception) {
                            log.error("Error while handle partial push sending success", e)
                        }

                        return pushMessageService.save(pushMessage.apply { sent = true })
                    }
                    else -> {
                        val errorMessage = """
                         Send push to Huawei topic failed! 
                         Huawei API response: $response
                    """.trimIndent()
                        log.error(errorMessage)
                        pushMessageService.save(pushMessage.apply { sent = false })
                        throw ScpServerException(errorMessage)
                    }
                }
            } else {
                val errorMessage = """
                    Send push to Huawei topic failed! 
                    Http code: ${result.code()}, http errorBody: ${result.errorBody()?.string()}
                    """.trimIndent()
                log.error(errorMessage)
                pushMessageService.save(pushMessage.apply { sent = false })
                throw ScpServerException(errorMessage)
            }
        } catch (e: Exception) {
            log.error(e.message, e)
            pushMessageService.save(pushMessage.apply { sent = false })
            throw ScpServerException(e.message, e)
        }
    }

    private fun createDataString(data: Map<String, String>) =
            data.asIterable().joinToString(
                    prefix = "{",
                    postfix = "}",
                    separator = ", ",
                    transform = { (key, value) -> "'$key':'$value'" }
            )

    data class PushSendingPartialSuccess(
            val success: Int,
            val failure: Int,
            @JsonProperty("illegal_tokens")
            val illegalTokens: List<String>
    )
}