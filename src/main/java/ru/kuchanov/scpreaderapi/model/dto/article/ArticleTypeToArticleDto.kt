package ru.kuchanov.scpreaderapi.model.dto.article

import ru.kuchanov.scpreaderapi.ScpReaderConstants

data class ArticleTypeToArticleDto(
        val id: Long,
        val articleTypeId: Long,
        val titleForLang: String?,
        val enumValue: ScpReaderConstants.ArticleTypeEnum?
)
