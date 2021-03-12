package ru.kuchanov.scpreaderapi.controller.firebase

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.push.firebase.FirebaseMessagingService
import ru.kuchanov.scpreaderapi.service.push.PushMessageService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.FIREBASE + "/" + ScpReaderConstants.Path.MESSAGING)
class FirebaseMessagingController @Autowired constructor(
        val fcmService: FirebaseMessagingService,
        val pushMessageService: PushMessageService
) {

    @GetMapping("/send/topic")
    fun sendToTopic(
            @RequestParam(value = "topicName") topicName: String,
            @RequestParam(value = "type") type: ScpReaderConstants.Firebase.Fcm.MessageType,
            @RequestParam(value = "title") title: String,
            @RequestParam(value = "message") message: String,
            @RequestParam(value = "url") url: String?,
            @AuthenticationPrincipal user: User
    ) = fcmService.sendMessageToTopic(topicName, type, title, message, url, user)

    @GetMapping("/send/topic/{id}")
    fun sendToTopic(
            @PathVariable(value = "id") id: Long,
            @RequestParam(value = "topicName") topicName: String,
            @AuthenticationPrincipal user: User
    ) = fcmService.sendMessageToTopicById(topicName, id, user)

    @GetMapping("/all/byTypes")
    fun getAllByTypes(
            @RequestParam(value = "types") types: List<ScpReaderConstants.Firebase.Fcm.MessageType>
    ) = pushMessageService.findAllByTypeIn(types)

    @GetMapping("/delete/{id}")
    fun getAllByTypes(
            @PathVariable(value = "id") id: Long
    ) = pushMessageService.deleteById(id)
}
