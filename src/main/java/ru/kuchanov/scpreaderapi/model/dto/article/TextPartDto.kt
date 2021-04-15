package ru.kuchanov.scpreaderapi.model.dto.article

import ru.kuchanov.scpreaderapi.service.parse.article.TextType


data class TextPartDto(
        val id: Long,
        val data: String?,
        val type: TextType
) {
    var innerTextParts: List<TextPartDto> = listOf()
}
