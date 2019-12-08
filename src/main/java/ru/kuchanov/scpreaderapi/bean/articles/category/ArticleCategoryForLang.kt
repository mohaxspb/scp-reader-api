package ru.kuchanov.scpreaderapi.bean.articles.category

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "article_categories__to__langs")
data class ArticleCategoryForLang(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "article_category_id")
        val articleCategoryId: Long,
        @Column(name = "lang_id")
        val langId: String,
        @Column(name = "title", columnDefinition = "TEXT")
        val title: String,
        @Column(name = "site_url", columnDefinition = "TEXT")
        val siteUrl: String,
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)


@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ArticleCategoryForLangNotFoundException(
        override val message: String? = "ArticleCategoryForLang not found in db!"
) : RuntimeException(message)
