package ru.kuchanov.scpreaderapi.repository.firebase.push

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.firebase.push.PushMessage

interface PushMessageRepository : JpaRepository<PushMessage, Long> {

    fun findAllByTypeInOrderByCreatedDesc(
            types: List<ScpReaderConstants.Push.MessageType>
    ): List<PushMessage>

    @Query(
            """
                select * from push_messages
                where type in :types
                and (user_id = :userId or user_id IS NULL) 
                order by created desc
            """,
            nativeQuery = true
    )
    fun findAllByTypeInAndUserIdOrderByCreatedDesc(
            types: List<String>,
            userId: Long
    ): List<PushMessage>

    fun findAllByUserId(userId: Long): List<PushMessage>
}