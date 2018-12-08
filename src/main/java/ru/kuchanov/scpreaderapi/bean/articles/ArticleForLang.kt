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
//                ,
//                ColumnResult(name = "imageUrls")
            ])
])
@NamedNativeQuery(
        name = "ArticleForLang.getMostRecentArticlesForLang",
        resultSetMapping = "ArticleInListDtoResult",
        //            image_urls as imageUrls,
        //NULL as imageUrls,
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
)

data class ArticleForLang(
        @Id
        @Column(name = "article_id")
        var articleId: Long? = null,
        @Id
        @Column(name = "lang_id")
        var langId: String,
        var title: String?,
        @Id
        @Column(name = "url_relative")
        var urlRelative: String,
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