package ru.kuchanov.scpreaderapi.repository.article.read

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticlesByLang

interface ReadArticlesForLangRepository : JpaRepository<ReadArticlesByLang, Long> {

    fun findByArticleToLangIdAndUserId(articleToLangId: Long, userId: Long): ReadArticlesByLang?
}
