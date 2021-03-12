package ru.kuchanov.scpreaderapi.service.push.huawei

import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.push.PushProviderMessagingService

@Service
class HuaweiMessagingService : PushProviderMessagingService {
    override fun sendMessageToTopic(
            topicName: String,
            type: ScpReaderConstants.Firebase.Fcm.MessageType,
            title: String,
            message: String,
            url: String?,
            author: User
    ): PushMessage {
        TODO("Not yet implemented")
    }

    override fun sendMessageToTopicById(
            topicName: String,
            pushMessageId: Long,
            author: User
    ): PushMessage {
        TODO("Not yet implemented")
    }
}