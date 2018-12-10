package ru.kuchanov.scpreaderapi.service.article

import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInList
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInListWithImages
import javax.transaction.Transactional

interface ArticleForLangService {

    fun findAll(): List<ArticleForLang>

    fun update(articleForLang: ArticleForLang): ArticleForLang

    @Transactional
    fun insert(articleForLang: ArticleForLang): ArticleForLang

    fun insert(articleForLang: List<ArticleForLang>): List<ArticleForLang>

    fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String): ArticleForLang?

    fun getArticleForLang(articleId: Long, langId: String): ArticleForLang?

    fun getMostRecentArticlesForLang(langId: String, offset: Int, limit: Int): List<ArticleInList>

    fun getOneByLangAndArticleId(articleId: Long, langId: String): ArticleForLang?

    //test
    fun getMostRecentTest(langId: String, offset: Int, limit: Int): List<ArticleInListWithImages>

    //test
    fun getMostRecentTest2(langId: String, offset: Int, limit: Int): List<ArticleForLang>
}