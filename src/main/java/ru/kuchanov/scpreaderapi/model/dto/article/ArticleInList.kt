package ru.kuchanov.scpreaderapi.model.dto.article

import ru.kuchanov.scpreaderapi.bean.articles.ArticlesImages
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
        val articleId: Long,
        val langId: String,
        val urlRelative: String,
        val title: String,
        val rating: Int
){
    var imageUrls: List<ArticlesImages>? = null
}