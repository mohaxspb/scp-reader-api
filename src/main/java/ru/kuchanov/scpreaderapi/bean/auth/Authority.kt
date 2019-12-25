package ru.kuchanov.scpreaderapi.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "authorities")
@NoArgConstructor
data class Authority(
        @Id
        @Enumerated(EnumType.STRING)
        val authority: AuthorityType,
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
) : Serializable

enum class AuthorityType {
    USER, ADMIN, BANNER
}
