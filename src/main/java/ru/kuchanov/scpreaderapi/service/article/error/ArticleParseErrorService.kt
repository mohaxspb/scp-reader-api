package ru.kuchanov.scpreaderapi.service.article.error

import ru.kuchanov.scpreaderapi.bean.articles.error.ArticleParseError

interface ArticleParseErrorService {

    fun save(error: ArticleParseError): ArticleParseError

    fun findAllByLangIdAndUrlRelative(langId: String, urlRelative: String): List<ArticleParseError>
}
