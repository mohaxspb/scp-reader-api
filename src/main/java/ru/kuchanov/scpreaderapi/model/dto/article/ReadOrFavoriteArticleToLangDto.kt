package ru.kuchanov.scpreaderapi.model.dto.article

import java.sql.Timestamp


data class ReadOrFavoriteArticleToLangDto(
        val id: Long,
        val statusChangedDate: Timestamp,
        val articleToLangId: Long,
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
    var articleTypeDto: ArticleTypeDto? = null
}
