package ru.kuchanov.scpreaderapi.service.article.favorite

import ru.kuchanov.scpreaderapi.bean.articles.favorite.FavoriteArticleByLang
import ru.kuchanov.scpreaderapi.model.dto.article.ReadOrFavoriteArticleToLangDto
import javax.transaction.Transactional

interface FavoriteArticleForLangService {

    @Transactional
    fun deleteById(id: Long)

    @Transactional
    fun save(article: FavoriteArticleByLang): FavoriteArticleByLang

    fun getFavoriteArticleForArticleIdLangIdAndUserId(
            articleToLangId: Long,
            userId: Long
    ): FavoriteArticleByLang?

    fun findByArticleToLangIdAndUserId(articleToLangId: Long, userId: Long): FavoriteArticleByLang?

    fun findAllByUserIdAndLangId(
            userId: Long,
            langId: String? = null,
            offset: Int,
            limit: Int
    ): List<ReadOrFavoriteArticleToLangDto>
}
