package ru.kuchanov.scpreaderapi.bean.articles.read

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.sql.Timestamp
import javax.persistence.*


@Entity
@Table(name = "read__articles_to_lang__to__users")
@NoArgConstructor
data class ReadArticlesByLang(
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
