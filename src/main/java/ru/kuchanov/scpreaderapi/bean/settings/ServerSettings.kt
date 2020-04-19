package ru.kuchanov.scpreaderapi.bean.settings

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "server_settings")
data class ServerSettings(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        val key: String,
        val value: String,

        @Column(name = "author_id")
        var authorId: Long? = null,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
) {

    enum class Key {
        HOURLY_SYNC_TASK_ENABLED
    }
}


@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such ServerSettings")
class ServerSettingsNotFoundException : RuntimeException()