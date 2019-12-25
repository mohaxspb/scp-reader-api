package ru.kuchanov.scpreaderapi.bean.articles.types

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "article__to__article_type")
@NoArgConstructor
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
