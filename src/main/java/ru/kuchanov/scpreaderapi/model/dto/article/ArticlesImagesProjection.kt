package ru.kuchanov.scpreaderapi.model.dto.article

interface ArticlesImagesProjection {
    val id: Long
    val url: String
    val articleForLangId: Long
}


fun ArticlesImagesProjection.toDto() =
        ArticlesImagesDto(id = this.id, url = this.url)