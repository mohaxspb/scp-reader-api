package ru.kuchanov.scpreaderapi.bean.articles.favorite

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "favorite__articles_to_lang__to__users")
@NoArgConstructor
data class FavoriteArticlesByLang(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @Column(name = "user_id")
        var userId: Long,
        @Column(name = "article_to_lang_id")
        var articleToLangId: Long,

        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)
