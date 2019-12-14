package ru.kuchanov.scpreaderapi.service.article

import ru.kuchanov.scpreaderapi.bean.articles.favorite.FavoriteArticlesByLang
import javax.transaction.Transactional

interface FavoriteArticleForLangService {
    fun findAll(): List<FavoriteArticlesByLang>

    fun update(articleForLang: FavoriteArticlesByLang): FavoriteArticlesByLang

    @Transactional
    fun insert(article: FavoriteArticlesByLang): FavoriteArticlesByLang

    fun insert(articles: List<FavoriteArticlesByLang>): List<FavoriteArticlesByLang>

    fun getFavoriteArticleForArticleIdLangIdAndUserId(
            articleId: Long,
            langId: String,
            userId: Long
    ): FavoriteArticlesByLang?
}