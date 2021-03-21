package ru.kuchanov.scpreaderapi.service.push

import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage

interface PushMessageService {

    fun findAllByTypeIn(types: List<ScpReaderConstants.Push.MessageType>, userId: Long?): List<PushMessage>

    fun findAllByUserId(userId: Long): List<PushMessage>

    fun findOneById(id: Long): PushMessage?

    fun deleteById(id: Long): Boolean

    fun save(pushMessage: PushMessage): PushMessage
}