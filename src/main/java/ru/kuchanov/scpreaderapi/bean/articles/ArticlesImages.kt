package ru.kuchanov.scpreaderapi.bean.articles

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*


@Entity
@IdClass(KeyArticlesImages::class)
@Table(name = "articles_images")
data class ArticlesImages(
//        @Id
//        @Column(name = "lang_id")
//        var langId: String? = null,
//        @Id
//        @Column(name = "lang_id")
//        var langId: String? = null,
//        @Id
//        @Column(name = "article_id")
//        var articleId: Long? = null,
//        @Id
//        @Column(name = "article_for_lang_id")
//        var articleForLangId: Long? = null,

//        @Id
//        var url: String?,
//        @Id
//        var keyArticleLangs: KeyArticleLangs?=null,

        @Id
        @Column(name = "article_url_relative")
        var articleUrlRelative: String? = null,
        @Id
        @Column(name = "article_lang_id")
        var articleLangId: String? = null,
        @Id
        @Column(name = "article_id")
        var articleId: Long? = null,
        @Id
        var url: String,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)

@NoArgConstructor
data class KeyArticlesImages(
        val articleUrlRelative: String?=null,
        val articleLangId: String?=null,
        val articleId: Long?=null,
        val url: String?=null
) : Serializable