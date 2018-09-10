package ru.kuchanov.scpreaderapi.repository.article

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.articles.KeyArticleLangs

interface ArticlesForLangRepository : JpaRepository<ArticleForLang, KeyArticleLangs> {
    @Query("SELECT al FROM ArticleForLang al " +
            "WHERE al.urlRelative = :urlRelative AND al.langId = :langId")
    fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String): ArticleForLang?
}