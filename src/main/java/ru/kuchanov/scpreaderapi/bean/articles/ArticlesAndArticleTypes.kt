package ru.kuchanov.scpreaderapi.bean.articles

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

@Entity
@IdClass(KeyArticleAndArticleType::class)
@Table(name = "articles_article_types")
data class ArticlesAndArticleTypes(
        @Id
        @Column(name = "article_id")
        var articleId: Long,
        @Id
        @Column(name = "article_type_id")
        var articleTypeId: Long,
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        @Version
        val updated: Timestamp? = null
)

@NoArgConstructor
data class KeyArticleAndArticleType(
        val articleId: Long,
        val articleTypeId: Long
) : Serializable