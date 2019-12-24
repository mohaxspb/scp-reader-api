package ru.kuchanov.scpreaderapi.bean.articles.error

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "article_parse_errors")
data class ArticleParseError(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "lang_id")
        val langId: String,
        @Column(name = "url_relative", columnDefinition = "TEXT")
        val urlRelative: String,
        @Column(name = "error_class", columnDefinition = "TEXT")
        val errorClass: String,
        @Column(name = "error_message", columnDefinition = "TEXT")
        val errorMessage: String?,
        @Column(name = "stacktrace", columnDefinition = "TEXT")
        val stacktrace: String,
        @Column(name = "cause_error_class", columnDefinition = "TEXT")
        var causeErrorClass: String? = null,
        @Column(name = "cause_error_message", columnDefinition = "TEXT")
        var causeErrorMessage: String? = null,
        @Column(name = "cause_stacktrace", columnDefinition = "TEXT")
        var causeStacktrace: String? = null,

        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)


@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ArticleCategoryNotFoundException(
        override val message: String? = "ArticleParseError not found in db!"
) : RuntimeException(message)
