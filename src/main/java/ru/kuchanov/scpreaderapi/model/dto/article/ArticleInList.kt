package ru.kuchanov.scpreaderapi.model.dto.article

import ru.kuchanov.scpreaderapi.utils.NoArgConstructor


/**
 * @param articleId
 * @param langId
 * @param urlRelative
 * @param title
 * @param rating
 */
@NoArgConstructor
data class ArticleInList(
        val id: Long,
        val articleId: Long,
        val langId: String,
        val urlRelative: String,
        val title: String,
        val rating: Int?
){
    var imageUrls: List<ArticlesImagesDto>? = null
    var tagsForLang: List<TagForLangDto>? = null
}
