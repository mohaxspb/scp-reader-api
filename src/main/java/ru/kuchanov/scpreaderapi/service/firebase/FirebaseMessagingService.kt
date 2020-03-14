package ru.kuchanov.scpreaderapi.service.firebase

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Firebase.Fcm
import ru.kuchanov.scpreaderapi.service.auth.UserToAuthorityService
import ru.kuchanov.scpreaderapi.service.users.LangService
import ru.kuchanov.scpreaderapi.service.users.UserService
import ru.kuchanov.scpreaderapi.service.users.UsersLangsService

@Service
class FirebaseMessagingService @Autowired constructor(
        val log: Logger,
        val userService: UserService,
        val userToAuthorityService: UserToAuthorityService,
        val langService: LangService,
        val usersLangsService: UsersLangsService
) {
    fun sendMessageToTopic(
            topicName: String,
            type: Fcm.MessageType,
            title: String,
            message: String,
            url: String?
    ): String {
        val data = mutableMapOf<String, String>()
        when (type) {
            Fcm.MessageType.MESSAGE -> {
                data[Fcm.DataParamName.TYPE.name] = type.name
                data[Fcm.DataParamName.MESSAGE.name] = message
                data[Fcm.DataParamName.TITLE.name] = title
            }
            Fcm.MessageType.EXTERNAL_URL -> TODO()
            Fcm.MessageType.NEW_VERSION -> TODO()
        }

        val fcmMessage = Message.builder()
                .putAllData(data)
                .setTopic(topicName)
                .build()

        return FirebaseMessaging.getInstance().send(fcmMessage)
    }
}
