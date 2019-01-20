package ru.kuchanov.scpreaderapi.bean.articles

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*


@Entity
@Table(name = "articles_langs_to_articles_langs")
data class ArticleForLangToArticleForLang(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        //relations
        @Column(name = "parent_article_for_lang_id")
        var parentArticleForLangId: Long,
        @Column(name = "article_for_lang_id")
        var articleForLangId: Long,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)