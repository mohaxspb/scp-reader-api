package ru.kuchanov.scpreaderapi.repository.article

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInList

interface ArticlesForLangRepository : JpaRepository<ArticleForLang, Long> {

    @Query("SELECT al FROM ArticleForLang al " +
            "WHERE al.urlRelative = :urlRelative AND al.langId = :langId")
    fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String): ArticleForLang?

    @Query(
            "SELECT al FROM ArticleForLang al " +
                    "WHERE al.articleId = :articleId AND al.langId = :langId")
    fun getArticleForLang(articleId: Long, langId: String): ArticleForLang?

    @Query(nativeQuery = true)
    fun getMostRecentArticlesForLang(langId: String, offset: Int, limit: Int): List<ArticleInList>

    @Query(
            value = """
            SELECT * FROM articles_langs a
            WHERE a.lang_id = :langId AND a.created_on_site IS NOT NULL
            ORDER BY a.created_on_site DESC
            OFFSET :offset LIMIT :limit
            """,
            nativeQuery = true
    )
    fun getMostRecentArticlesForLangFull(langId: String, offset: Int, limit: Int): List<ArticleForLang>

    fun getOneByArticleIdAndLangId(articleId: Long, langId: String): ArticleForLang?
}