package ru.kuchanov.scpreaderapi.service.push.firebase

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Push
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessageNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.exception.ScpServerException
import ru.kuchanov.scpreaderapi.service.push.PushMessageService
import ru.kuchanov.scpreaderapi.service.push.PushProviderMessagingService
import java.time.format.DateTimeFormatter

@Service
class FirebaseMessagingService @Autowired constructor(
        val log: Logger,
        val pushMessageService: PushMessageService
) : PushProviderMessagingService {

    override fun sendMessageToTopic(
            topicName: String,
            type: Push.MessageType,
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
            pushMessageId: Long,
            author: User
    ): PushMessage {
        val savedMessage = pushMessageService.findOneById(pushMessageId) ?: throw PushMessageNotFoundException()

        return sendMessageToTopic(topicName, savedMessage)
    }

    private fun sendMessageToTopic(
            topicName: String,
            pushMessage: PushMessage
    ): PushMessage {
        val fcmMessage = fcmMessageFromPushMessage(pushMessage, topicName = topicName)

        try {
            FirebaseMessaging.getInstance().send(fcmMessage)
            return pushMessageService.save(pushMessage.apply { sent = true })
        } catch (e: Exception) {
            e.printStackTrace()
            log.error(e.message)
            pushMessageService.save(pushMessage.apply { sent = false })
            throw ScpServerException(e.message, e)
        }
    }

    private fun fcmMessageFromPushMessage(pushMessage: PushMessage, token: String? = null, topicName: String? = null): Message? {
        if (token != null && topicName != null) {
            throw ScpServerException(message = "Use only topic or token, not both!")
        }
        if (token.isNullOrEmpty() && topicName.isNullOrEmpty()) {
            throw ScpServerException(message = "Topic and token are null or empty!")
        }

        val data = mutableMapOf<String, String>()
        data[Push.DataParamName.ID.name] = pushMessage.id!!.toString()
        when (pushMessage.type) {
            Push.MessageType.MESSAGE -> {
            }
            Push.MessageType.EXTERNAL_URL, Push.MessageType.NEW_VERSION -> {
                data[Push.DataParamName.URL.name] = pushMessage.url!!
            }
        }
        data[Push.DataParamName.TYPE.name] = pushMessage.type.name
        data[Push.DataParamName.MESSAGE.name] = pushMessage.message
        data[Push.DataParamName.TITLE.name] = pushMessage.title
        data[Push.DataParamName.UPDATED.name] = DateTimeFormatter.ISO_INSTANT.format(pushMessage.updated!!.toInstant())

        return Message.builder()
                .putAllData(data)
                .apply {
                    token?.let { setToken(token) }
                    topicName?.let { setTopic(it) }
                }
                .build()
    }
}
