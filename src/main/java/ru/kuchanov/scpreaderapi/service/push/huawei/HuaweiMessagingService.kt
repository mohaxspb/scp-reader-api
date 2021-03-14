package ru.kuchanov.scpreaderapi.service.push.huawei

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessageNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.push.PushMessageService
import ru.kuchanov.scpreaderapi.service.push.PushProviderMessagingService

@Service
class HuaweiMessagingService @Autowired constructor(
        val log: Logger,
        val pushMessageService: PushMessageService
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
        TODO("Not yet implemented")
    }
}