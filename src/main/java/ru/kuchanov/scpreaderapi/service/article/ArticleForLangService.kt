package ru.kuchanov.scpreaderapi.service.article

import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleToLangDto
import javax.transaction.Transactional

interface ArticleForLangService {

    fun getOneByLangIdAndArticleId(articleId: Long, langId: String): ArticleForLang?

    fun getOneByLangIdAndArticleIdAsDto(articleId: Long, langId: String): ArticleToLangDto?

    fun getOneByIdAsDto(id: Long): ArticleToLangDto?

    fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String): ArticleForLang?

    fun getArticleForLangByUrlRelativeAndLangAsDto(urlRelative: String, langId: String): ArticleToLangDto?

    fun getIdByUrlRelativeAndLangId(urlRelative: String, langId: String): Long?

    @Transactional
    fun save(articleForLang: ArticleForLang): ArticleForLang

    fun getCreatedArticleToLangsBetweenDates(startDate: String, endDate: String): List<ArticleToLangDto>

    fun getMostRecentArticlesForLang(langId: String, offset: Int, limit: Int): List<ArticleToLangDto>

    fun getMostRatedArticlesForLang(langId: String, offset: Int, limit: Int): List<ArticleToLangDto>

    fun findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLangId: Long): List<ArticleToLangDto>

    fun findAllByIdsWithTextParts(articleToLangIds: List<Long>): List<ArticleToLangDto>

    fun getRandomArticle(langId: String?): ArticleToLangDto

    fun findIdsByArticleIds(articleIds: List<Long>): List<Long>

    fun deleteByIds(ids: List<Long>)
}
