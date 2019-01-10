package ru.kuchanov.scpreaderapi.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

@Entity
@IdClass(KeyUserAuthority::class)
@Table(name = "authorities")
data class Authority(
        @Id
        @Column(name = "user_id")
        var userId: Long?,
        @Id
        @Column(name = "authority")
        var authority: String,
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
) : Serializable

enum class AuthorityType {
    USER, ADMIN, BANNER
}