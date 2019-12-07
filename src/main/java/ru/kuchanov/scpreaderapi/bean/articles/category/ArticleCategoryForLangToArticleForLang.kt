package ru.kuchanov.scpreaderapi.bean.articles.category

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "article_categories_to_lang__to__articles_to_lang")
data class ArticleCategoryForLangToArticleForLang(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "article_category_to_lang_id")
        val articleCategoryToLangId: Long,
        @Column(name = "article_to_lang_id")
        val articleForLangId: Long,
        @Column(name = "order_in_category")
        val orderInCategory: Int,
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)
