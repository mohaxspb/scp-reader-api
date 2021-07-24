package ru.kuchanov.scpreaderapi.service.push.firebase

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MulticastMessage
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
import ru.kuchanov.scpreaderapi.service.push.UserToPushTokensService

@Service
class FirebaseMessagingService @Autowired constructor(
        private val log: Logger,
        private val pushMessageService: PushMessageService,
        private val userToPushTokensService: UserToPushTokensService
) : PushProviderMessagingService {

    override fun sendMessageToUser(
            userId: Long,
            type: Push.MessageType,
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

        return sendMessageToUser(savedMessage, userId)
    }

    override fun sendMessageToUserById(
            userId: Long,
            pushMessageId: Long,
            author: User
    ): PushMessage {
        val savedMessage = pushMessageService.findOneById(pushMessageId) ?: throw PushMessageNotFoundException()

        return sendMessageToUser(savedMessage, userId)
    }

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
            pushMessageId: Long
    ): PushMessage {
        val savedMessage = pushMessageService.findOneById(pushMessageId) ?: throw PushMessageNotFoundException()

        return sendMessageToTopic(topicName, savedMessage)
    }

    private fun sendMessageToUser(pushMessage: PushMessage, userId: Long): PushMessage {
        val tokens = userToPushTokensService.findByUserIdAndPushTokenProvider(
                userId,
                Push.Provider.GOOGLE
        ).map { it.pushTokenValue }

        val fcmMessage = fcmMulticastMessageFromPushMessage(pushMessage, tokens)

        try {
            val result = FirebaseMessaging.getInstance().sendMulticast(fcmMessage)

            //handle failures
            if (tokens.size == result.responses.size) {
                result.responses.forEachIndexed { index, _ ->
                    try {
                        userToPushTokensService.deleteByPushTokenValue(tokens[index])
                    } catch (e: Exception) {
                        log.error("Error while handle partial push sending success", e)
                    }
                }
            }
            if (result.successCount > 0) {
                return pushMessageService.save(pushMessage.apply { sent = true })
            } else {
                val firstError = result.responses.first { it.isSuccessful.not() }.exception
                pushMessageService.save(pushMessage.apply { sent = false })
                throw ScpServerException(firstError.message, firstError)
            }
        } catch (e: Exception) {
            log.error(e.message, e)
            pushMessageService.save(pushMessage.apply { sent = false })
            throw ScpServerException(e.message, e)
        }
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
            log.error(e.message, e)
            pushMessageService.save(pushMessage.apply { sent = false })
            throw ScpServerException(e.message, e)
        }
    }

    private fun fcmMessageFromPushMessage(
            pushMessage: PushMessage,
            topicName: String
    ): Message {
        if (topicName.isEmpty()) {
            throw ScpServerException(message = "Topic is null or empty!")
        }

        return Message.builder()
                .putAllData(pushMessageToMap(pushMessage))
                .setTopic(topicName)
                .build()
    }

    private fun fcmMulticastMessageFromPushMessage(
            pushMessage: PushMessage,
            tokens: List<String>
    ): MulticastMessage {
        if (tokens.isEmpty()) {
            throw ScpServerException(message = "Tokens list is empty!")
        }

        return MulticastMessage.builder()
                .putAllData(pushMessageToMap(pushMessage))
                .addAllTokens(tokens)
                .build()
    }
}
