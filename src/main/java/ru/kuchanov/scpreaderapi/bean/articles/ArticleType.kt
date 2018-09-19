package ru.kuchanov.scpreaderapi.bean.articles

import javax.persistence.*

@Entity
@Table(name = "article_types",
        indexes = [
                Index(
                        name = "index_article_types_id",
                        columnList = "id",
                        unique = true
                )
        ]
)
data class ArticleType(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "image_url")
        val imageUrl:String
)