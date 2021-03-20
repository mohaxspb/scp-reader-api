package ru.kuchanov.scpreaderapi.service.push

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Push
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessageNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.isAdmin
import ru.kuchanov.scpreaderapi.model.exception.ScpAccessDeniedException
import ru.kuchanov.scpreaderapi.service.push.firebase.FirebaseMessagingService
import ru.kuchanov.scpreaderapi.service.push.huawei.HuaweiMessagingService

@Service
class AllProvidersMessagingServiceImpl @Autowired constructor(
        private val log: Logger,
        private val pushMessageService: PushMessageService,
        private val huaweiMessagingService: HuaweiMessagingService,
        private val firebaseMessagingService: FirebaseMessagingService
) : AllProvidersMessagingService {

    override fun sendMessageToTopic(
            topicName: String,
            type: Push.MessageType,
            title: String,
            message: String,
            url: String?,
            author: User
    ): List<PushSendResult> {
        //check is admin
        if (author.isAdmin().not()) {
            log.error("Attempt to send push by non admin user! User: $author")
            throw ScpAccessDeniedException()
        }

        val savedMessage = pushMessageService.save(
                PushMessage(
                        topicName = topicName,
                        type = type,
                        title = title,
                        message = message,
                        url = url,
                        authorId = author.id!!
                )
        )

        val firebaseResult: PushSendResult = try {
            PushSendResult.Success(
                    provider = Push.Provider.GOOGLE,
                    pushMessage = firebaseMessagingService.sendMessageToTopic(
                            topicName,
                            type,
                            title,
                            message,
                            url,
                            author
                    )
            )
        } catch (e: Throwable) {
            PushSendResult.Fail(provider = Push.Provider.GOOGLE, error = e, pushMessage = savedMessage)
        }

        val huaweiResult: PushSendResult = try {
            PushSendResult.Success(
                    provider = Push.Provider.HUAWEI,
                    pushMessage = huaweiMessagingService.sendMessageToTopic(
                            topicName,
                            type,
                            title,
                            message,
                            url,
                            author
                    )
            )
        } catch (e: Throwable) {
            PushSendResult.Fail(provider = Push.Provider.HUAWEI, error = e, pushMessage = savedMessage)
        }

        return listOf(firebaseResult, huaweiResult)
    }

    override fun sendMessageToTopicById(
            topicName: String,
            pushMessageId: Long,
            author: User
    ): List<PushSendResult> {
        //check is admin
        if (author.isAdmin().not()) {
            log.error("Attempt to send push by non admin user! User: $author")
            throw ScpAccessDeniedException()
        }

        val savedMessage = pushMessageService.findOneById(pushMessageId) ?: throw PushMessageNotFoundException()

        val firebaseResult: PushSendResult = try {
            PushSendResult.Success(
                    provider = Push.Provider.GOOGLE,
                    pushMessage = firebaseMessagingService.sendMessageToTopicById(
                            topicName,
                            savedMessage.id!!
                    )
            )
        } catch (e: Throwable) {
            PushSendResult.Fail(provider = Push.Provider.GOOGLE, error = e, pushMessage = savedMessage)
        }

        val huaweiResult: PushSendResult = try {
            PushSendResult.Success(
                    provider = Push.Provider.HUAWEI,
                    pushMessage = huaweiMessagingService.sendMessageToTopicById(
                            topicName,
                            savedMessage.id!!
                    )
            )
        } catch (e: Throwable) {
            PushSendResult.Fail(provider = Push.Provider.HUAWEI, error = e, pushMessage = savedMessage)
        }

        return listOf(firebaseResult, huaweiResult)
    }

        return listOf(firebaseResult)
    }
}
