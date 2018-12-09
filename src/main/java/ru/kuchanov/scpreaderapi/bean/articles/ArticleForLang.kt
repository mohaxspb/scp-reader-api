package ru.kuchanov.scpreaderapi.bean.articles

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInList
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*


@Entity
@IdClass(KeyArticleLangs::class)
@Table(name = "articles_langs")

@SqlResultSetMapping(name = "ArticleInListDtoResult", classes = [
    ConstructorResult(targetClass = ArticleInList::class,
            columns = [
                ColumnResult(name = "articleId", type = Long::class),
                ColumnResult(name = "langId"),
                ColumnResult(name = "urlRelative"),
                ColumnResult(name = "title"),
                ColumnResult(name = "rating", type = Int::class)
            ])
])
@NamedNativeQuery(
        name = "ArticleForLang.getMostRecentArticlesForLang",
        resultSetMapping = "ArticleInListDtoResult",
        query = """
            SELECT
            article_id as articleId,
            lang_id as langId,
            url_relative as urlRelative,
            title,
            rating
            FROM articles_langs a
            WHERE a.lang_id = :langId AND a.created_on_site IS NOT NULL
            ORDER BY a.created_on_site DESC
            OFFSET :offset LIMIT :limit
             """
        // try this:
//        query = """
//            SELECT
//            article_id as articleId,
//            lang_id as langId,
//            url_relative as urlRelative,
//            title,
//            rating,
//			jsonb_agg((SELECT art_id FROM (SELECT ar.id) art_id)) AS imageUrls
//            FROM articles_langs a
//			INNER JOIN articles ar ON a.article_id = ar.id
//            WHERE a.lang_id = 'ru' AND a.created_on_site IS NOT NULL
//			GROUP BY a.article_id, a.lang_id, url_relative
//            ORDER BY a.created_on_site DESC
//            OFFSET 0 LIMIT 1
//        """
)

data class ArticleForLang(
        @Id
        @Column(name = "article_id")
        var articleId: Long? = null,
        @Id
        @Column(name = "lang_id")
        var langId: String,
        @Id
        @Column(name = "url_relative")
        var urlRelative: String,
        var title: String?,
        //new ones
        var text: String? = null,
        var rating: Int? = null,
        @Column(name = "comments_url")
        var commentsUrl: String? = null,
        @Column(name = "created_on_site")
        var createdOnSite: Timestamp? = null,
        @Column(name = "updated_on_site")
        var updatedOnSite: Timestamp? = null,
        //todo add fields

        @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
        @JoinColumns(
                value = [
//                    JoinColumn(name = "article_Id", referencedColumnName = "article_Id"),
//                    JoinColumn(name = "lang_Id", referencedColumnName = "article_Lang_Id"),
//                    JoinColumn(name = "url_Relative", referencedColumnName = "article_Url_Relative")

                    JoinColumn(name = "article_Id", referencedColumnName = "article_id"),
                    JoinColumn(name = "article_lang_Id", referencedColumnName = "lang_id"),
                    JoinColumn(name = "article_Url_Relative", referencedColumnName = "url_relative")
                ]
        )
        var images: List<ArticlesImages>? = null,

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