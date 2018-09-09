package ru.kuchanov.scpreaderapi.bean.articles

import javax.persistence.*

@Entity
@Table(name = "article_types")
data class ArticleType(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "image_url")
        val imageUrl:String
)