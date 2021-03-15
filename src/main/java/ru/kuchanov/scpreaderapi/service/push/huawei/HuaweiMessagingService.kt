package ru.kuchanov.scpreaderapi.service.push.huawei

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessageNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.configuration.HuaweiApiConfiguration.Companion.HUAWEI_PUSH_SENDING_SUCCESS_CODE
import ru.kuchanov.scpreaderapi.model.exception.ScpServerException
import ru.kuchanov.scpreaderapi.model.huawei.push.HuaweiPushMessage
import ru.kuchanov.scpreaderapi.network.HuaweiPushApi
import ru.kuchanov.scpreaderapi.service.push.PushMessageService
import ru.kuchanov.scpreaderapi.service.push.PushProviderMessagingService
import java.net.HttpURLConnection

@Service
class HuaweiMessagingService @Autowired constructor(
        private val log: Logger,
        private val pushMessageService: PushMessageService,
        private val huaweiPushApi: HuaweiPushApi,
        @Value("\${my.api.huawei.client_id}") private val huaweiClientId: String
) : PushProviderMessagingService {

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

        return sendMessageToTopic(topicName, savedMessage)
    }

    override fun sendMessageToTopicById(
            topicName: String,
            pushMessageId: Long
    ): PushMessage {
        val savedMessage = pushMessageService.findOneById(pushMessageId) ?: throw PushMessageNotFoundException()

        return sendMessageToTopic(topicName, savedMessage)
    }

    private fun sendMessageToTopic(
            topicName: String,
            pushMessage: PushMessage
    ): PushMessage {
        val data = pushMessageToMap(pushMessage).asIterable().joinToString(
                prefix = "{",
                postfix = "}",
                separator = ", ",
                transform = { (key, value) -> "'$key':'$value'" }
        )

        try {
            val result = huaweiPushApi.send(
                    applicationId = huaweiClientId,
                    huaweiPushMessage = HuaweiPushMessage(
                            message = HuaweiPushMessage.Message(
                                    data = data,
                                    topic = topicName
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
                if (response.code == HUAWEI_PUSH_SENDING_SUCCESS_CODE) {
                    return pushMessageService.save(pushMessage.apply { sent = true })
                } else {
                    val errorMessage = """
                         Send push to Huawei topic failed! 
                         Huawei API response: $response
                    """.trimIndent()
                    log.error(errorMessage)
                    pushMessageService.save(pushMessage.apply { sent = false })
                    throw ScpServerException(errorMessage)
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
}