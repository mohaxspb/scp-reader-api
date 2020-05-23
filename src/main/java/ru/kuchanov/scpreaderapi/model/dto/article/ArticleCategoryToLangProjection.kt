package ru.kuchanov.scpreaderapi.model.dto.article


interface ArticleCategoryToLangProjection {
    val id: Long
    val articleCategoryId: Long
    val defaultTitle: String
    val title: String
    val articlesCount: Int
    val siteUrl: String?
}
