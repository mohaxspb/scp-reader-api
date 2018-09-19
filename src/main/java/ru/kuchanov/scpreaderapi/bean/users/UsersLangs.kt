package ru.kuchanov.scpreaderapi.bean.users

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*

@Entity
@IdClass(KeyUserLang::class)
@Table(name = "users_langs",
        indexes = [
            Index(name = "index_users_langs_ids", columnList = "user_id,lang_id", unique = true)
        ]
)
data class UsersLangs(
        @Id
        @Column(name = "user_id")
        var userId: Long,
        @Id
        @Column(name = "lang_id")
        var langId: String,
        @Column(name = "firebase_uid")
        var firebaseUid: String,
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        @Version
        val updated: Timestamp? = null
)