package ru.kuchanov.scpreaderapi.repository.article.read

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticleByLang

interface ReadArticlesForLangRepository : JpaRepository<ReadArticleByLang, Long> {

    fun findByArticleToLangIdAndUserId(articleToLangId: Long, userId: Long): ReadArticleByLang?
}
