package ru.kuchanov.scpreaderapi.service.firebase.push

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage
import ru.kuchanov.scpreaderapi.repository.firebase.push.PushMessageRepository

@Service
class PushMessageServiceImpl @Autowired constructor(
        private val pushMessageRepository: PushMessageRepository
) : PushMessageService {

    override fun findAllByTypeIn(types: List<ScpReaderConstants.Firebase.Fcm.MessageType>): List<PushMessage> =
            pushMessageRepository.findAllByTypeIn(types)

    override fun findAllByUserId(userId: Long): List<PushMessage> =
            pushMessageRepository.findAllByUserId(userId)

    override fun findOneById(id: Long): PushMessage? =
            pushMessageRepository.findByIdOrNull(id)

    override fun deleteById(id: Long): Boolean =
            pushMessageRepository.deleteById(id).let { true }

    override fun save(pushMessage: PushMessage): PushMessage =
            pushMessageRepository.save(pushMessage)
}