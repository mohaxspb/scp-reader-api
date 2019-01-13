package ru.kuchanov.scpreaderapi.bean.articles

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInList
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*


@Entity
@Table(
        name = "articles_langs",
        uniqueConstraints = [
            UniqueConstraint(
                    columnNames = ["article_id", "lang_id", "url_relative"]
            )
        ]
)

@SqlResultSetMapping(
        name = "ArticleInListDtoResult",
        classes = [
            ConstructorResult(
                    targetClass = ArticleInList::class,
                    columns = [
                        ColumnResult(name = "id", type = Long::class),
                        ColumnResult(name = "articleId", type = Long::class),
                        ColumnResult(name = "langId"),
                        ColumnResult(name = "urlRelative"),
                        ColumnResult(name = "title"),
                        ColumnResult(name = "rating", type = Int::class)
                    ])
        ]
)
@NamedNativeQuery(
        name = "ArticleForLang.getMostRecentArticlesForLang",
        resultSetMapping = "ArticleInListDtoResult",
        query = """
            SELECT
            id,
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
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @Column(name = "article_id")
        var articleId: Long? = null,
        @Column(name = "lang_id")
        var langId: String,
        @Column(name = "url_relative")
        var urlRelative: String,
        @Column(columnDefinition = "TEXT")
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
                    JoinColumn(name = "article_for_lang_id", referencedColumnName = "id")
                ]
        )
        var images: MutableSet<ArticlesImages> = mutableSetOf(),

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
) : Serializable


@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such articleForLang")
class ArticleForLangNotFoundException : RuntimeException()