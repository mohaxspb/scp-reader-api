package ru.kuchanov.scpreaderapi.bean.articles

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import javax.persistence.*

@Entity
@Table(
        name = "articles",
        indexes = [
                Index(name = "index_articles_id", columnList = "id", unique = true),
                Index(name = "index_articles_articleTypeId", columnList = "article_type_id")
        ]
)
data class Article(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "article_type_id")
        val articleTypeId: Long? = null
)

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such article")
class ArticleNotFoundException : RuntimeException()