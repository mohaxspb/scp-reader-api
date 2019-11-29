package ru.kuchanov.scpreaderapi.bean.articles

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "articles_article_types",
        indexes = [
            Index(
                    name = "index_articles_article_types_ids",
                    columnList = "article_id,article_type_id",
                    unique = true
            )
        ]
)
data class ArticlesAndArticleTypes(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "article_id")
        var articleId: Long,
        @Column(name = "article_type_id")
        var articleTypeId: Long,
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)
