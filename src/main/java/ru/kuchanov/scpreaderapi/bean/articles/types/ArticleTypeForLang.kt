package ru.kuchanov.scpreaderapi.bean.articles.types

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "article_types__to__langs")
@NoArgConstructor
data class ArticleTypeForLang(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "article_type_id")
        var articleTypeId: Long,
        @Column(name = "lang_id")
        var langId: String,
        @Column(name = "title", columnDefinition = "TEXT")
        var title: String,
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)
