package ru.kuchanov.scpreaderapi.controller.firebase

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.service.firebase.push.FirebaseMessagingService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.FIREBASE + "/" + ScpReaderConstants.Path.MESSAGING)
class FirebaseMessagingController @Autowired constructor(
        val fcmService: FirebaseMessagingService
) {

    @GetMapping("/send/topic")
    fun sendToTopic(
            @RequestParam(value = "topicName") topicName: String,
            @RequestParam(value = "type") type: ScpReaderConstants.Firebase.Fcm.MessageType,
            @RequestParam(value = "title") title: String,
            @RequestParam(value = "message") message: String,
            @RequestParam(value = "url") url: String?
    ) = fcmService.sendMessageToTopic(topicName, type, title, message, url)
}
