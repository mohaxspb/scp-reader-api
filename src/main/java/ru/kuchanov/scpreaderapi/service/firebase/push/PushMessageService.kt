package ru.kuchanov.scpreaderapi.service.firebase.push

import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage

interface PushMessageService {

    fun findAllByTypeIn(types: List<ScpReaderConstants.Firebase.Fcm.MessageType>): List<PushMessage>

    fun findAllByUserId(userId: Long): List<PushMessage>

    fun deleteById(id: Long): Boolean

    fun save(pushMessage: PushMessage): PushMessage
}