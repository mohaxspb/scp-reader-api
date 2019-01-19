package ru.kuchanov.scpreaderapi.bean.articles.tags

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForLang.Companion.NATIVE_QUERY_ALL_FOR_LANG_ID_AND_ARTICLE_FOR_LANG_ID
import java.sql.Timestamp
import javax.persistence.*


@Entity
@Table(
        name = "tags_langs",
        uniqueConstraints = [UniqueConstraint(columnNames = ["lang_id", "title"])]
)
@NamedNativeQueries(
        value = [
            NamedNativeQuery(
                    name = NATIVE_QUERY_ALL_FOR_LANG_ID_AND_ARTICLE_FOR_LANG_ID,
                    query =
                    """
                        SELECT tl.* FROM tags_langs tl
                        JOIN tags_articles_langs tal
                        ON tal.tag_for_lang_id = tl.id AND tal.article_for_lang_id = :articleForLangId
                        WHERE tl.lang_id = :langId
                    """
            )
        ]
)
data class TagForLang(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        //relations
        @Column(name = "tag_id")
        var tagId: Long? = null,
        @Column(name = "lang_id")
        var langId: String,

        //content
        @Column(columnDefinition = "TEXT")
        var title: String,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
) {
    companion object {
        const val NATIVE_QUERY_ALL_FOR_LANG_ID_AND_ARTICLE_FOR_LANG_ID = "NATIVE_QUERY_ALL_FOR_LANG_ID_AND_ARTICLE_FOR_LANG_ID"
    }
}