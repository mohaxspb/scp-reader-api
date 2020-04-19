package ru.kuchanov.scpreaderapi.bean.articles

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "articles")
data class Article(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such article")
class ArticleNotFoundException : RuntimeException()
