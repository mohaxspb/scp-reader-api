package ru.kuchanov.scpreaderapi.controller.push

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.push.AllProvidersMessagingService
import ru.kuchanov.scpreaderapi.service.push.PushMessageService
import ru.kuchanov.scpreaderapi.service.push.PushSendResult


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.PUSH + "/" + ScpReaderConstants.Path.MESSAGING)
class PushMessagingController @Autowired constructor(
        val pushMessageService: PushMessageService,
        val allProvidersMessagingService: AllProvidersMessagingService
) {

    @GetMapping("/send/topic")
    fun sendToTopic(
            @RequestParam(value = "topicName") topicName: String,
            @RequestParam(value = "type") type: ScpReaderConstants.Push.MessageType,
            @RequestParam(value = "title") title: String,
            @RequestParam(value = "message") message: String,
            @RequestParam(value = "url") url: String?,
            @AuthenticationPrincipal user: User
    ): List<PushSendResult> =
            allProvidersMessagingService.sendMessageToTopic(topicName, type, title, message, url, user)

    @GetMapping("/send/topic/{id}")
    fun sendToTopic(
            @PathVariable(value = "id") id: Long,
            @RequestParam(value = "topicName") topicName: String,
            @AuthenticationPrincipal user: User
    ): List<PushSendResult> = allProvidersMessagingService.sendMessageToTopicById(topicName, id, user)

    @GetMapping("/all/byTypes")
    fun getAllByTypes(
            @RequestParam(value = "types") types: List<ScpReaderConstants.Push.MessageType>
    ) = pushMessageService.findAllByTypeIn(types)

    @GetMapping("/delete/{id}")
    fun delete(
            @PathVariable(value = "id") id: Long
    ) = pushMessageService.deleteById(id)
}
