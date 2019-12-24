package ru.kuchanov.scpreaderapi.repository.article.error

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.error.ArticleParseError

interface ArticleParseErrorRepository : JpaRepository<ArticleParseError, Long> {

    fun findAllByLangIdAndUrlRelative(langId: String, urlRelative: String): List<ArticleParseError>
}
