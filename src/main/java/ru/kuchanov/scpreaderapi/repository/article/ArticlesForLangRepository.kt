package ru.kuchanov.scpreaderapi.repository.article

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInList

interface ArticlesForLangRepository : JpaRepository<ArticleForLang, Long> {

    fun findByUrlRelativeAndLangId(urlRelative: String, langId: String): ArticleForLang?

    @Query(
            """
                SELECT al.id FROM ArticleForLang al
                WHERE al.urlRelative = :urlRelative AND al.langId = :langId
                """
    )
    fun getIdByUrlRelativeAndLangId(urlRelative: String, langId: String): Long?

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

    @Query(
            value = """
                select * from articles_langs art
                join article_categories_to_lang__to__articles_to_lang art_cat on art.id = art_cat.article_to_lang_id
                where art.id in 
                    (select article_to_lang_id from article_categories_to_lang__to__articles_to_lang 
                        where article_category_to_lang_id = :articleCategoryToLangId order by order_in_category)
            """,
            nativeQuery = true
    )
    fun findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLangId: Long): List<ArticleForLang>

    fun getOneByArticleIdAndLangId(articleId: Long, langId: String): ArticleForLang?
}
