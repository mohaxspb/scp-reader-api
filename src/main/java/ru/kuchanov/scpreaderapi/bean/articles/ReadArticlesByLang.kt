package ru.kuchanov.scpreaderapi.bean.articles

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

@Entity
@IdClass(KeyReadArticleByLang::class)
@Table(name = "read_articles_by_lang")
data class ReadArticlesByLang(
        @Id
        @Column(name = "user_id")
        var userId: Long,
        @Id
        @Column(name = "article_id")
        var articleId: Long,
        @Id
        @Column(name = "lang_id")
        var langId: String,
        @Column(name = "is_read")
        var isRead: Boolean = false,
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)

@NoArgConstructor
data class KeyReadArticleByLang(
        val userId: Long,
        val articleId: Long,
        val langId: String
) : Serializable