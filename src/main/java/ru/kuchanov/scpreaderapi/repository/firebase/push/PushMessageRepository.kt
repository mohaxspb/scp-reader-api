package ru.kuchanov.scpreaderapi.repository.firebase.push

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage

interface PushMessageRepository : JpaRepository<PushMessage, Long> {

    fun findAllByTypeInOrderByCreatedDesc(types: List<ScpReaderConstants.Firebase.Fcm.MessageType>): List<PushMessage>

    fun findAllByUserId(userId: Long): List<PushMessage>
}