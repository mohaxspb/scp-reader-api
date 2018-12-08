package ru.kuchanov.scpreaderapi.model.dto.article

import ru.kuchanov.scpreaderapi.utils.NoArgConstructor


/**
 * @param articleId
 * @param langId
 * @param urlRelative
 * @param imageUrls
 * @param title
 * @param rating
 */
@NoArgConstructor
data class ArticleInList(
        val articleId: Long? = null,
        val langId: String? = null,
        val urlRelative: String? = null,
        val title: String? = null,
        val rating: Int? = null
//        val imageUrls: List<String>? = null
//        val imageUrls: String? = null
){
    val imageUrls: List<String>? = null
}