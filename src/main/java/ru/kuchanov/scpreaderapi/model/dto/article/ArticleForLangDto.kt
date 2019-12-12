package ru.kuchanov.scpreaderapi.model.dto.article


data class ArticleForLangDto(
        val id: Long,
        val articleId: Long,
        val langId: String,
        val urlRelative: String,
        val title: String,
        val rating: Int?
) {
    var imageUrls: List<ArticlesImagesDto>? = null
    var tagsForLang: List<TagForLangDto>? = null
    var articleTypeDto: ArticleTypeDto? = null
}
