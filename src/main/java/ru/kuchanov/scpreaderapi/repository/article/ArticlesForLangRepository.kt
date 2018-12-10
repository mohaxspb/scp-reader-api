package ru.kuchanov.scpreaderapi.repository.article

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.articles.KeyArticleLangs
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInList
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInListWithImages

interface ArticlesForLangRepository : JpaRepository<ArticleForLang, KeyArticleLangs> {

    @Query("SELECT al FROM ArticleForLang al " +
            "WHERE al.urlRelative = :urlRelative AND al.langId = :langId")
    fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String): ArticleForLang?

    @Query(
            "SELECT al FROM ArticleForLang al " +
                    "WHERE al.articleId = :articleId AND al.langId = :langId")
    fun getArticleForLang(articleId: Long, langId: String): ArticleForLang?

    @Query(nativeQuery = true)
    fun getMostRecentArticlesForLang(langId: String, offset: Int, limit: Int): List<ArticleInList>

    fun getOneByArticleIdAndLangId(articleId: Long, langId: String): ArticleForLang?

    @Query(
//            value = """
//            SELECT
//            *
//            FROM articles_langs a
//            WHERE a.lang_id = :langId AND a.created_on_site IS NOT NULL
//            ORDER BY a.created_on_site DESC
//            OFFSET :offset LIMIT :limit
//             """,
            value = """
            SELECT
            article_id as article_Id,
            lang_id as lang_id,
            url_relative as url_relative,
            title as title,
            rating as rating
            FROM articles_langs a
            WHERE a.lang_id = :langId AND a.created_on_site IS NOT NULL
            ORDER BY a.created_on_site DESC
            OFFSET :offset LIMIT :limit
             """,
//            value = """
//            SELECT
//            article_id as articleId,
//            lang_id as langId,
//            url_relative as urlRelative,
//            title as title,
//            rating as rating
//            FROM articles_langs a
//            WHERE a.lang_id = :langId AND a.created_on_site IS NOT NULL
//            ORDER BY a.created_on_site DESC
//            OFFSET :offset LIMIT :limit
//             """,
            nativeQuery = true
    )
    fun getMostRecentTest(langId: String, offset: Int, limit: Int): List<ArticleInListWithImages>

    @Query(
//            value = """
//            SELECT
//            *
//            FROM articles_langs a
//            WHERE a.lang_id = :langId AND a.created_on_site IS NOT NULL
//            ORDER BY a.created_on_site DESC
//            OFFSET :offset LIMIT :limit
//             """,
            value = """
            SELECT
            article_id as articleId,
            lang_id as articleLangId,
            url_relative as articleUrlRelative,
            title as title,
            rating as rating
            FROM articles_langs a
            WHERE a.lang_id = :langId AND a.created_on_site IS NOT NULL
            ORDER BY a.created_on_site DESC
            OFFSET :offset LIMIT :limit
             """,
            nativeQuery = true
    )
    fun getMostRecentTest2(langId: String, offset: Int, limit: Int): List<ArticleForLang>

    //cant use offset/limit
    @Query(
            value = """
                select
                a FROM ArticleForLang a JOIN FETCH a.images i
                WHERE a.langId = :langId AND a.createdOnSite IS NOT NULL
                ORDER BY a.createdOnSite DESC
            """
    )
    fun getMostRecentTest3(
            langId: String
//            , offset: Int,
//            limit: Int
    ):
    List<ArticleInListWithImages>
//            List<ArticleForLang>
}