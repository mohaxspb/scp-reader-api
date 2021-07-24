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
    @Qualifier(Application.HUAWEI_LOGGER) private val huaweiLog: Logger,
    @Qualifier(Application.GOOGLE_LOGGER) private val googleLog: Logger,
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

    override fun sendToUser(
            userId: Long,
            title: String,
            message: String,
            type: Push.MessageType,
            author: User
    ): List<PushSendResult> {
        checkNotNull(author.id)
        //check is admin
        if (author.isAdmin().not()) {
            log.error("Attempt to send push by non admin user! User: $author")
            throw ScpAccessDeniedException()
        }

        val savedMessage = pushMessageService.save(
                PushMessage(
                        userId = userId,
                        type = type,
                        title = title,
                        message = message,
                        url = null,
                        authorId = author.id
                )
        )

        val firebaseResult: PushSendResult = try {
            PushSendResult.Success(
                    provider = Push.Provider.GOOGLE,
                    pushMessage = firebaseMessagingService.sendMessageToUserById(
                            userId = userId,
                            pushMessageId = savedMessage.id!!,
                            author = author
                    )
            )
        } catch (e: Throwable) {
            PushSendResult.Fail(provider = Push.Provider.GOOGLE, error = e, pushMessage = savedMessage)
        }

        val huaweiResult: PushSendResult = try {
            PushSendResult.Success(
                    provider = Push.Provider.HUAWEI,
                    pushMessage = huaweiMessagingService.sendMessageToUserById(
                            userId = userId,
                            pushMessageId = savedMessage.id!!,
                            author = author
                    )
            )
        } catch (e: Throwable) {
            PushSendResult.Fail(provider = Push.Provider.HUAWEI, error = e, pushMessage = savedMessage)
        }

        return listOf(firebaseResult, huaweiResult)
    }

    override fun printPushSendResults(pushSendResults: List<PushSendResult>) {
        pushSendResults.forEach {
            val logger = when (it.provider) {
                Push.Provider.GOOGLE -> {
                    googleLog
                }
                Push.Provider.HUAWEI -> {
                    huaweiLog
                }
            }
            if (it is PushSendResult.Success) {
                logger.info("Push send successfully!")
            } else if (it is PushSendResult.Fail) {
                logger.error("Push send failed!", it.error)
            }
        }
    }
}
