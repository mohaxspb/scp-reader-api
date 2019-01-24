package ru.kuchanov.scpreaderapi.bean.articles

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*


@Entity
@IdClass(KeyArticleLangs::class)
@Table(name = "articles_langs")
data class ArticleForLang(
        @Id
        @Column(name = "article_id")
        var articleId: Long,
        @Id
        @Column(name = "lang_id")
        var langId: String,
        var title: String?,
        @Id
        @Column(name = "url_relative")
        var urlRelative: String,
        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)

@NoArgConstructor
data class KeyArticleLangs(
        val articleId: Long? = null,
        val langId: String? = null,
        val urlRelative: String? = null
) : Serializable


@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such articleForLang")
class ArticleForLangNotFoundException : RuntimeException()