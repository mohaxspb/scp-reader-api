package ru.kuchanov.scpreaderapi.service.article

import ru.kuchanov.scpreaderapi.bean.articles.Article
import javax.transaction.Transactional

interface ArticleService {

    fun getById(id: Long): Article

    @Transactional
    fun insert(article: Article): Article

    fun getArticleByUrlRelative(urlRelative: String): Article?

    fun getArticleByUrlRelativeAndLang(urlRelative: String, langId: String): Article?
}
