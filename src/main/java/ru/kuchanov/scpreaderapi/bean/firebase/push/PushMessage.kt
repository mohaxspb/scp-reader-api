package ru.kuchanov.scpreaderapi.bean.firebase.push

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.sql.Timestamp
import javax.persistence.*

@Entity
@NoArgConstructor
@Table(name = "push_messages")
data class PushMessage(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        //content
        @Column(name = "topic_name")
        val topicName: String? = null,
        @Column(name = "user_id")
        val userId: Long? = null,
        @Enumerated(EnumType.STRING)
        @Column(name = "type")
        val type: ScpReaderConstants.Firebase.Fcm.MessageType,
        @Column(name = "title", columnDefinition = "TEXT")
        val title: String,
        @Column(name = "message", columnDefinition = "TEXT")
        val message: String,
        val url: String? = null,
        //state
        var sent: Boolean = true,
        //author
        @Column(name = "author_id")
        val authorId: Long,
        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such PushMessage")
class PushMessageNotFoundException : RuntimeException()