package ru.kuchanov.scpreaderapi.service.push

import ru.kuchanov.scpreaderapi.ScpReaderConstants.Firebase.Fcm
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage
import ru.kuchanov.scpreaderapi.bean.users.User

interface PushProviderMessagingService {
    fun sendMessageToTopic(
            topicName: String,
            type: Fcm.MessageType,
            title: String,
            message: String,
            url: String?,
            author: User
    ): PushMessage

    fun sendMessageToTopicById(
            topicName: String,
            pushMessageId: Long,
            author: User
    ): PushMessage
}
