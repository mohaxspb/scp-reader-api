package ru.kuchanov.scpreaderapi.model.dto.article


interface ArticleInListProjection {

    val id: Long
    val articleId: Long
    val langId: String
    val urlRelative: String
    val title: String
    val rating: Int?
}
