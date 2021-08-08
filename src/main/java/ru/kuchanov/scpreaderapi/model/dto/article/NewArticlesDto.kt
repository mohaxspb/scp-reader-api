package ru.kuchanov.scpreaderapi.model.dto.article

data class NewArticlesDto(
    val langId: String,
    val newArticles: List<NewArticleDto>
) {
    data class NewArticleDto(
        val id: Long,
        val title: String
    )
}