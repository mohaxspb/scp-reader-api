package ru.kuchanov.scpreaderapi.service.article

import ru.kuchanov.scpreaderapi.bean.articles.Article
import javax.transaction.Transactional

interface ArticleService {
    fun findAll(): List<Article>
    fun getById(id: Long): Article
    fun update(article: Article): Article

    @Transactional
    fun insert(article: Article): Article

    fun insert(articles: List<Article>): List<Article>

    fun getArticleByUrlRelativeAndLang(urlRelative: String, langId: String): Article?

//    fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String): ArticleForLang?
}