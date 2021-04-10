package ru.kuchanov.scpreaderapi.model.dto.article

data class TagDto(
        val tagId: Long,
        val tagToLangId: Long,
        val tagToLangToArticleToLangId: Long,
        val title: String,
)
