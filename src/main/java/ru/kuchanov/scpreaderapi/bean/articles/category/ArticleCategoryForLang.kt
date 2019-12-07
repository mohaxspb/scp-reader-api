package ru.kuchanov.scpreaderapi.bean.articles.category

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
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
