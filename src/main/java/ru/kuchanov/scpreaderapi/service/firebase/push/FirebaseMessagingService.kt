package ru.kuchanov.scpreaderapi.service.firebase.push

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Firebase.Fcm
import ru.kuchanov.scpreaderapi.bean.auth.AuthorityType
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessageNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.exception.ScpAccessDeniedException
import ru.kuchanov.scpreaderapi.model.exception.ScpServerException

@Service
class FirebaseMessagingService @Autowired constructor(
        val log: Logger,
        val pushMessageService: PushMessageService
) {
    fun sendMessageToTopic(
            topicName: String,
            type: Fcm.MessageType,
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

        return sendMessageToTopic(topicName, savedMessage, author)
    }

    fun sendMessageToTopicById(
            topicName: String,
            pushMessageId: Long,
            author: User
    ): PushMessage {
        val savedMessage = pushMessageService.findOneById(pushMessageId) ?: throw PushMessageNotFoundException()

        return sendMessageToTopic(topicName, savedMessage, author)
    }

    private fun sendMessageToTopic(
            topicName: String,
            pushMessage: PushMessage,
            author: User
    ): PushMessage {
        //check is admin
        if (!author.userAuthorities.map { it.authority }.contains(AuthorityType.ADMIN)) {
            throw ScpAccessDeniedException()
        }

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
        when (pushMessage.type) {
            Fcm.MessageType.MESSAGE -> {
            }
            Fcm.MessageType.EXTERNAL_URL, Fcm.MessageType.NEW_VERSION -> {
                data[Fcm.DataParamName.URL.name] = pushMessage.url!!
            }
        }
        data[Fcm.DataParamName.TYPE.name] = pushMessage.type.name
        data[Fcm.DataParamName.MESSAGE.name] = pushMessage.message
        data[Fcm.DataParamName.TITLE.name] = pushMessage.title

        return Message.builder()
                .putAllData(data)
                .apply {
                    token?.let { setToken(token) }
                    topicName?.let { setTopic(it) }
                }
                .build()
    }
}
