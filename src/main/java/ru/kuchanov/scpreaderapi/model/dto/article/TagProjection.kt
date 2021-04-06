package ru.kuchanov.scpreaderapi.model.dto.article

interface TagProjection {
    val tagId: Long
    val tagToLangId: Long
    val tagToLangToArticleToLangId: Long
    val title: String
    val articleToLangId: Long
}

fun TagProjection.toDto() =
        TagDto(
                tagId = tagId,
                tagToLangId = tagToLangId,
                tagToLangToArticleToLangId = tagToLangToArticleToLangId,
                title = title
        )