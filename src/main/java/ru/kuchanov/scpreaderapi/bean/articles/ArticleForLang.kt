package ru.kuchanov.scpreaderapi.bean.articles

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInList
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

@NamedNativeQueries(
        value = [
            NamedNativeQuery(
                    name = "ArticleForLang.getMostRecentArticlesForLang",
                    resultSetMapping = "ArticleInListDtoResult",
                    query =
                    """
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
        ]
)

data class ArticleForLang(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @Column(name = "article_id")
        var articleId: Long? = null,
        @Column(name = "lang_id")
        var langId: String,
        @Column(name = "url_relative", columnDefinition = "TEXT")
        var urlRelative: String,
        @Column(columnDefinition = "TEXT")
        var title: String?,
        //new ones
        @Column(columnDefinition = "TEXT")
        var text: String? = null,
        var rating: Int? = null,
        @Column(name = "comments_url", columnDefinition = "TEXT")
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

        //seems to be we do not need this field at all...
        //as we cant insert automatically...
        //but we can just hold info in this field to handle it during parsing
        //so we do need anything except of @Transient...
        @ManyToMany(
                //doesnt work as it's crashing on unique constraints
                //cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH],
                //this also doest work, for the same reason
                //cascade = [CascadeType.ALL],
                //and without it we'll get another error - try to write not managed object...
                fetch = FetchType.EAGER
        )
        @JoinTable(
                name = "tags_articles_langs",
                joinColumns = [
                    JoinColumn(name = "article_for_lang_id", referencedColumnName = "id")
                ],
                inverseJoinColumns = [
                    JoinColumn(name = "tag_for_lang_id", referencedColumnName = "id")
                ],
                uniqueConstraints = [
                    UniqueConstraint(
                            columnNames = [
                                "tag_for_lang_id",
                                "article_for_lang_id"
                            ]
                    )
                ]
        )
        //to not write on insert
        @Transient
        var tags: MutableSet<TagForLang> = mutableSetOf(),

        //inner articles
        //todo check it and create migration

        /**
         * see https://stackoverflow.com/a/13708470/3212712
         */
        @ManyToMany(mappedBy = "parentArticlesForLang")
        @JoinTable(
                name = "articles_langs_to_articles_langs",
                joinColumns = [
                    JoinColumn(name = "parent_article_for_lang_id", referencedColumnName = "id")
                ],
                inverseJoinColumns = [
                    JoinColumn(name = "article_for_lang_id", referencedColumnName = "id")
                ]
        )
        //to not write on insert
        @Transient
        val innerArticlesForLang: Set<ArticleForLang> = mutableSetOf(),

        /**
         * see https://stackoverflow.com/a/13708470/3212712
         */
        @ManyToMany()
        @JoinTable(
                name = "articles_langs_to_articles_langs",
                joinColumns = [
                    JoinColumn(name = "article_for_lang_id", referencedColumnName = "id")
                ],
                inverseJoinColumns = [
                    JoinColumn(name = "parent_article_for_lang_id", referencedColumnName = "id")
                ]
        )
        //to not write on insert
        @Transient
        val parentArticlesForLang: Set<ArticleForLang> = mutableSetOf(),

//@ManyToMany(mappedBy = "following", cascade = CascadeType.ALL)
//@JoinTable(name="UserRel",
//                joinColumns={@JoinColumn(name="ParentId")},
//                inverseJoinColumns={@JoinColumn(name="UserId")})
//private Set<User> followers = new HashSet<User>();
//
//@ManyToMany(cascade = CascadeType.ALL)
//@JoinTable(name="UserRel",
//                joinColumns={@JoinColumn(name="UserId")},
//                inverseJoinColumns={@JoinColumn(name="ParentId")})
//private Set<User> following = new HashSet<User>();

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such articleForLang")
class ArticleForLangNotFoundException : RuntimeException()