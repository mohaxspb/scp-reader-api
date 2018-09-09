package ru.kuchanov.scpreaderapi.bean.articles

import javax.persistence.*

@Entity
@Table(name = "articles")
data class Article(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "article_type_id")
        val articleTypeId: Long
)