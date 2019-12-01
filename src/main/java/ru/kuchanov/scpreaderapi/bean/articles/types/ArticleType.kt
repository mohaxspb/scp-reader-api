package ru.kuchanov.scpreaderapi.bean.articles.types

import ru.kuchanov.scpreaderapi.ScpReaderConstants
import javax.persistence.*

@Entity
@Table(name = "article_types")
data class ArticleType(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "image_url", columnDefinition = "TEXT")
        val imageUrl: String,
        @Enumerated(EnumType.STRING)
        @Column(name = "enum_value", columnDefinition = "TEXT")
        val enumValue: ScpReaderConstants.ArticleTypeEnum
)
