package ru.kuchanov.scpreaderapi.bean.users

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "users_langs")
@NoArgConstructor
data class UsersLangs(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "user_id")
        var userId: Long,
        @Column(name = "lang_id")
        var langId: String,
        @Column(name = "firebase_uid", columnDefinition = "TEXT")
        var firebaseUid: String? = null,
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)
