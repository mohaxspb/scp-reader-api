package ru.kuchanov.scpreaderapi.model.dto.article

import ru.kuchanov.scpreaderapi.ScpReaderConstants

data class ArticleTypeDto(
        val articleTypeId: Long,
        val titleForLang: String?,
        val enumValue: ScpReaderConstants.ArticleTypeEnum?
)
