package ru.kuchanov.scpreaderapi.repository.article

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.read.KeyReadArticleByLang
import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticlesByLang

interface ReadArticlesForLangRepository : JpaRepository<ReadArticlesByLang, KeyReadArticleByLang> {
    @Query("SELECT ra FROM ReadArticlesByLang ra " +
            "WHERE ra.articleId = :articleId AND ra.langId = :langId AND ra.userId = :userId")
    fun getReadArticleForArticleIdLangIdAndUserId(articleId: Long, langId: String, userId: Long): ReadArticlesByLang?
}
