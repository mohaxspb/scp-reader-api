package ru.kuchanov.scpreaderapi.model.dto.article

import ru.kuchanov.scpreaderapi.ScpReaderConstants

interface ArticleTypeToArticleProjection {
    val id: Long
    val articleTypeId: Long
    val articleId: Long
    val titleForLang: String
    val enumValue: ScpReaderConstants.ArticleTypeEnum
}

fun ArticleTypeToArticleProjection.toDto() =
        ArticleTypeToArticleDto(
                id = id,
                articleTypeId = articleTypeId,
                titleForLang = titleForLang,
                enumValue = enumValue
        )
