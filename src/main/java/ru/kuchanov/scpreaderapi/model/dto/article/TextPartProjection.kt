package ru.kuchanov.scpreaderapi.model.dto.article

import ru.kuchanov.scpreaderapi.service.parse.article.TextType


interface TextPartProjection {
    val id: Long
    val data: String?
    val type: TextType
}
