package ru.kuchanov.scpreaderapi.bean.articles.category

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "article_categories")
data class ArticleCategory(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "default_title", columnDefinition = "TEXT")
        val defaultTitle: String,
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)


@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ArticleCategoryNotFoundException(
        override val message: String? = "ArticleCategory not found in db!"
) : RuntimeException(message)
