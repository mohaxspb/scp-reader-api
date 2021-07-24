package ru.kuchanov.scpreaderapi.service.push

import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage
import ru.kuchanov.scpreaderapi.bean.users.User

interface AllProvidersMessagingService {

    fun sendMessageToTopic(
            topicName: String,
            type: ScpReaderConstants.Push.MessageType,
            title: String,
            message: String,
            url: String?,
            author: User
    ): List<PushSendResult>

    fun sendMessageToTopicById(
            topicName: String,
            pushMessageId: Long,
            author: User
    ): List<PushSendResult>

    fun sendToUser(
            userId: Long,
            title: String,
            message: String,
            type: ScpReaderConstants.Push.MessageType,
            author: User
    ): List<PushSendResult>

    fun printPushSendResults(pushSendResults: List<PushSendResult>)
}

sealed class PushSendResult(open val provider: ScpReaderConstants.Push.Provider) {
    class Success(
            override val provider: ScpReaderConstants.Push.Provider,
            val pushMessage: PushMessage
    ) : PushSendResult(provider)

    class Fail(
            override val provider: ScpReaderConstants.Push.Provider,
            val pushMessage: PushMessage,
            val error: Throwable
    ) : PushSendResult(provider)
}

