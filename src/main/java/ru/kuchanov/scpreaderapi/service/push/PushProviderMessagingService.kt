package ru.kuchanov.scpreaderapi.service.push

import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage
import ru.kuchanov.scpreaderapi.bean.users.User
import java.time.format.DateTimeFormatter

interface PushProviderMessagingService {

    fun sendMessageToTopic(
            topicName: String,
            type: ScpReaderConstants.Push.MessageType,
            title: String,
            message: String,
            url: String?,
            author: User
    ): PushMessage

    fun sendMessageToTopicById(
            topicName: String,
            pushMessageId: Long
    ): PushMessage

    fun pushMessageToMap(pushMessage: PushMessage): Map<String, String> {
        val data = mutableMapOf<String, String>()
        data[ScpReaderConstants.Push.DataParamName.ID.name] = pushMessage.id!!.toString()
        when (pushMessage.type) {
            ScpReaderConstants.Push.MessageType.MESSAGE -> {
            }
            ScpReaderConstants.Push.MessageType.EXTERNAL_URL,
            ScpReaderConstants.Push.MessageType.NEW_VERSION -> {
                data[ScpReaderConstants.Push.DataParamName.URL.name] = pushMessage.url!!
            }
        }
        data[ScpReaderConstants.Push.DataParamName.TYPE.name] = pushMessage.type.name
        data[ScpReaderConstants.Push.DataParamName.MESSAGE.name] = pushMessage.message
        data[ScpReaderConstants.Push.DataParamName.TITLE.name] = pushMessage.title
        data[ScpReaderConstants.Push.DataParamName.UPDATED.name] =
                DateTimeFormatter.ISO_INSTANT.format(pushMessage.updated!!.toInstant())
        return data
    }
}
