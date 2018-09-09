package ru.kuchanov.scpreaderapi.bean.articles

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*


@Entity
@IdClass(KeyArticleLangs::class)
@Table(name = "articles_langs")
data class ArticlesLangs(
        @Id
        @Column(name = "article_id")
        var articleId: Long,
        @Id
        @Column(name = "lang_id")
        var langId: String,
        var title: String?,
        @Column(name = "url_relative")
        var urlRelative: String,
        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        @Version
        val updated: Timestamp? = null
)

@NoArgConstructor
data class KeyArticleLangs(
        val articleId: Long,
        val langId: String
) : Serializable
