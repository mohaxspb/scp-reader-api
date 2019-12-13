package ru.kuchanov.scpreaderapi.service.article

import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleForLangDto
import javax.transaction.Transactional

interface ArticleForLangService {

    fun getOneByLangAndArticleId(articleId: Long, langId: String): ArticleForLang?

    fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String): ArticleForLang?

    fun getIdByUrlRelativeAndLangId(urlRelative: String, langId: String): Long?

    @Transactional
    fun insert(articleForLang: ArticleForLang): ArticleForLang

    fun insert(articleForLang: List<ArticleForLang>): List<ArticleForLang>

    fun getMostRecentArticlesForLang(langId: String, offset: Int, limit: Int): List<ArticleForLangDto>

    fun getMostRatedArticlesForLang(langId: String, offset: Int, limit: Int): List<ArticleForLangDto>

    fun findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLangId: Long): List<ArticleForLangDto>
}
