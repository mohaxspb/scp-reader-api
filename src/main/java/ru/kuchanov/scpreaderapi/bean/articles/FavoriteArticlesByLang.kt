package ru.kuchanov.scpreaderapi.bean.articles

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

@Entity
@IdClass(KeyFavoriteArticleByLang::class)
@Table(name = "favorite_articles_by_lang")
data class FavoriteArticlesByLang(
        @Id
        @Column(name = "user_id")
        var userId: Long,
        @Id
        @Column(name = "article_id")
        var articleId: Long,
        @Id
        @Column(name = "lang_id")
        var langId: String,
        @Column(name = "is_favorite")
        var isFavorite: Boolean = false,
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)

@NoArgConstructor
data class KeyFavoriteArticleByLang(
        val userId: Long? = null,
        val articleId: Long? = null,
        val langId: String? = null
) : Serializable