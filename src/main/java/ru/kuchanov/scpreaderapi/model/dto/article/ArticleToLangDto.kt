package ru.kuchanov.scpreaderapi.model.dto.article

import java.sql.Timestamp


data class ArticleToLangDto(
        val id: Long,
        val articleId: Long,
        val langId: String,
        val urlRelative: String,
        val title: String,
        val rating: Int?,
        val createdOnSite: Timestamp?,
        val hasIframeTag: Boolean
) {
    var imageUrls: List<ArticlesImagesDto>? = null
    var tagsForLang: List<TagForLangDto>? = null
    var articleTypeToArticleDto: ArticleTypeToArticleDto? = null
    var textParts: List<TextPartDto>? = null
}
