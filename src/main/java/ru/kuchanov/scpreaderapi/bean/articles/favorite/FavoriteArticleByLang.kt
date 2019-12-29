package ru.kuchanov.scpreaderapi.bean.articles.favorite

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "favorite__articles_to_lang__to__users")
@NoArgConstructor
data class FavoriteArticleByLang(
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


@ResponseStatus(value = HttpStatus.NOT_FOUND)
class FavoriteArticleByLangNotFoundException(
        override val message: String? = "No such favorite article!"
) : RuntimeException(message)

@ResponseStatus(value = HttpStatus.CONFLICT)
class FavoriteArticleByLangAlreadyExistsException(
        override val message: String? = "Favorite article already exists!"
) : RuntimeException(message)

