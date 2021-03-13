package ru.kuchanov.scpreaderapi.controller.push

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.push.PushMessageService
import ru.kuchanov.scpreaderapi.service.push.firebase.FirebaseMessagingService
import ru.kuchanov.scpreaderapi.service.push.huawei.HuaweiMessagingService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.PUSH + "/" + ScpReaderConstants.Path.MESSAGING)
class PushMessagingController @Autowired constructor(
        val fcmService: FirebaseMessagingService,
        val huaweiMessagingService: HuaweiMessagingService,
        val pushMessageService: PushMessageService
) {

    @GetMapping("/send/topic")
    fun sendToTopic(
            @RequestParam(value = "topicName") topicName: String,
            @RequestParam(value = "type") type: ScpReaderConstants.Push.MessageType,
            @RequestParam(value = "title") title: String,
            @RequestParam(value = "message") message: String,
            @RequestParam(value = "url") url: String?,
            @AuthenticationPrincipal user: User
    ) = fcmService.sendMessageToTopic(topicName, type, title, message, url, user) //todo use huawei

    @GetMapping("/send/topic/{id}")
    fun sendToTopic(
            @PathVariable(value = "id") id: Long,
            @RequestParam(value = "topicName") topicName: String,
            @AuthenticationPrincipal user: User
    ) = fcmService.sendMessageToTopicById(topicName, id, user) //todo use huawei

    @GetMapping("/all/byTypes")
    fun getAllByTypes(
            @RequestParam(value = "types") types: List<ScpReaderConstants.Push.MessageType>
    ) = pushMessageService.findAllByTypeIn(types)

    @GetMapping("/delete/{id}")
    fun delete(
            @PathVariable(value = "id") id: Long
    ) = pushMessageService.deleteById(id)
}
