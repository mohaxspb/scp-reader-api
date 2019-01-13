package ru.kuchanov.scpreaderapi.bean.articles.tags

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*


@Entity
@Table(
        name = "tags_articles_langs",
        uniqueConstraints = [
            UniqueConstraint(
                    columnNames = ["tag_for_lang_id", "article_for_lang_id"]
            )
        ]
)
data class TagForArticleForLang(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        //relations
        @Column(name = "tag_for_lang_id")
        var tagForLangId: Long,
        @Column(name = "article_for_lang_id")
        var articleForLangId: String,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)