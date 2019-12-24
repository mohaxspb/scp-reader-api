package ru.kuchanov.scpreaderapi.service.article.favorite

import ru.kuchanov.scpreaderapi.bean.articles.favorite.FavoriteArticlesByLang
import javax.transaction.Transactional

interface FavoriteArticleForLangService {

    fun deleteById(id: Long)

    @Transactional
    fun save(article: FavoriteArticlesByLang): FavoriteArticlesByLang

    fun getFavoriteArticleForArticleIdLangIdAndUserId(
            articleToLangId: Long,
            userId: Long
    ): FavoriteArticlesByLang?
}
