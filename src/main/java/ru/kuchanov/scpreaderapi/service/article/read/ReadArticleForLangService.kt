package ru.kuchanov.scpreaderapi.service.article.read

import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticlesByLang
import javax.transaction.Transactional

interface ReadArticleForLangService {
    fun findAll(): List<ReadArticlesByLang>

    fun update(articleForLang: ReadArticlesByLang): ReadArticlesByLang

    @Transactional
    fun insert(article: ReadArticlesByLang): ReadArticlesByLang

    fun insert(articles: List<ReadArticlesByLang>): List<ReadArticlesByLang>

    fun getReadArticleForArticleIdLangIdAndUserId(
            articleId: Long,
            langId: String,
            userId: Long
    ): ReadArticlesByLang?
}