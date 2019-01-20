package ru.kuchanov.scpreaderapi.service.article

import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLangToArticleForLang
import javax.transaction.Transactional

interface ArticleForLangToArticleForLangService {

    fun findAll(): List<ArticleForLangToArticleForLang>

    @Transactional
    fun insert(data: ArticleForLangToArticleForLang): ArticleForLangToArticleForLang

    fun insert(data: List<ArticleForLangToArticleForLang>): List<ArticleForLangToArticleForLang>

    fun getOneById(id: Long): ArticleForLangToArticleForLang?

    fun findByParentArticleForLangIdAndArticleForLangId(
            parentArticleForLangId: Long,
            articleForLangId: Long
    ): ArticleForLangToArticleForLang?

    /**
     * returns all parents for given articleForLangId
     */
    fun getAllByArticleForLangId(articleForLangId: Long): List<ArticleForLangToArticleForLang>

    /**
     * returns all inner articles for given articleForLangId
     */
    fun getAllByParentArticleForLangId(articleForLangId: Long): List<ArticleForLangToArticleForLang>
}