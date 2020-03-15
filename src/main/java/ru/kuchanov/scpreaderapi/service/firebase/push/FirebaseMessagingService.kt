package ru.kuchanov.scpreaderapi.service.firebase.push

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Firebase.Fcm
import ru.kuchanov.scpreaderapi.bean.auth.AuthorityType
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.exception.ScpAccessDeniedException
import ru.kuchanov.scpreaderapi.model.exception.ScpServerException
import ru.kuchanov.scpreaderapi.service.users.LangService

@Service
class FirebaseMessagingService @Autowired constructor(
        val langService: LangService,
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
        //check is admin
        if (!author.userAuthorities.map { it.authority }.contains(AuthorityType.ADMIN)) {
            throw ScpAccessDeniedException()
        }

        val data = mutableMapOf<String, String>()
        when (type) {
            Fcm.MessageType.MESSAGE -> {
                data[Fcm.DataParamName.TYPE.name] = type.name
                data[Fcm.DataParamName.MESSAGE.name] = message
                data[Fcm.DataParamName.TITLE.name] = title
            }
            Fcm.MessageType.EXTERNAL_URL -> {
                data[Fcm.DataParamName.TYPE.name] = type.name
                data[Fcm.DataParamName.MESSAGE.name] = message
                data[Fcm.DataParamName.TITLE.name] = title
                data[Fcm.DataParamName.URL.name] = url!!
            }
            Fcm.MessageType.NEW_VERSION -> {
                data[Fcm.DataParamName.TYPE.name] = type.name
                data[Fcm.DataParamName.MESSAGE.name] = message
                data[Fcm.DataParamName.TITLE.name] = title
                data[Fcm.DataParamName.URL.name] = url!!
            }
        }

        val fcmMessage = Message.builder()
                .putAllData(data)
                .setTopic(topicName)
                .build()

        try {
            FirebaseMessaging.getInstance().send(fcmMessage)
            return pushMessageService.save(
                    PushMessage(
                            topicName = topicName,
                            type = type,
                            title = title,
                            message = message,
                            url = url,
                            authorId = author.id!!
                    )
            )
        } catch (e: Exception) {
            throw ScpServerException(e.message, e)
        }
    }
}
