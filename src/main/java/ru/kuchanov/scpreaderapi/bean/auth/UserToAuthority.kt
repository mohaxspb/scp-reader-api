package ru.kuchanov.scpreaderapi.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "authorities__to__users")
data class UserToAuthority(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "user_id")
        val userId: Long? = null,
        @Enumerated(EnumType.STRING)
        val authority: AuthorityType,
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
) : Serializable
