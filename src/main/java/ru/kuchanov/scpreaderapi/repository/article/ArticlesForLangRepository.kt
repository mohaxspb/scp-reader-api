package ru.kuchanov.scpreaderapi.repository.article

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.Article
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInListProjection

interface ArticlesForLangRepository : JpaRepository<ArticleForLang, Long> {

    fun getOneByArticleIdAndLangId(articleId: Long, langId: String): ArticleForLang?

    @Query(
            value = """
                select 
                art.id, 
                art.lang_id as langId,
                art.article_id as articleId,
                art.url_relative as urlRelative,
                art.title,
                art.rating,
                art.created_on_site as createdOnSite,
                art.has_iframe_tag as hasIframeTag 
                from articles_langs art
                where art.article_id = :articleId and lang_id = :langId
            """,
            nativeQuery = true
    )
    fun getOneByArticleIdAndLangIdAsProjection(articleId: Long, langId: String): ArticleInListProjection?

    @Query(
            value = """
                select 
                art.id, 
                art.lang_id as langId,
                art.article_id as articleId,
                art.url_relative as urlRelative,
                art.title,
                art.rating,
                art.created_on_site as createdOnSite,
                art.has_iframe_tag as hasIframeTag 
                from articles_langs art
                where art.id = :id
            """,
            nativeQuery = true
    )
    fun getOneByIdAsProjection(id: Long): ArticleInListProjection?

    fun findByUrlRelativeAndLangId(urlRelative: String, langId: String): ArticleForLang?

    @Query(
            value = """
                select 
                art.id, 
                art.lang_id as langId,
                art.article_id as articleId,
                art.url_relative as urlRelative,
                art.title,
                art.rating,
                art.created_on_site as createdOnSite,
                art.has_iframe_tag as hasIframeTag 
                from articles_langs art
                where art.url_relative = :urlRelative and lang_id = :langId
            """,
            nativeQuery = true
    )
    fun findByUrlRelativeAndLangIdAsProjection(urlRelative: String, langId: String): ArticleInListProjection?

    @Query(
            """
                SELECT al.id FROM ArticleForLang al
                WHERE al.urlRelative = :urlRelative AND al.langId = :langId
                """
    )
    fun getIdByUrlRelativeAndLangId(urlRelative: String, langId: String): Long?

    @Query(
            """
                SELECT al.id FROM ArticleForLang al
                WHERE al.articleId in :articleIds
                """
    )
    fun getIdsByArticleIds(articleIds: List<Long>): List<Long>

    @Query(
            """
                SELECT
                    art.id,
                    art.article_id as articleId,
                    art.lang_id as langId,
                    art.url_relative as urlRelative,
                    art.title,
                    art.rating,
                    art.created_on_site as createdOnSite,
                    art.has_iframe_tag as hasIframeTag 
                FROM articles_langs art
                WHERE art.created >= CAST( :startDate AS timestamp) 
                AND art.created <= CAST( :endDate AS timestamp) 
                ORDER BY art.created
        """,
            nativeQuery = true
    )
    fun getCreatedArticlesBetweenDates(startDate: String, endDate: String): List<ArticleInListProjection>

    @Query(
            value =
            """
                SELECT
                art.id,
                art.article_id as articleId,
                art.lang_id as langId,
                art.url_relative as urlRelative,
                art.title,
                art.rating,
                art.created_on_site as createdOnSite,
                art.has_iframe_tag as hasIframeTag 
                FROM articles_langs art
                WHERE art.lang_id = :langId AND art.created_on_site IS NOT NULL
                ORDER BY art.created_on_site DESC
                OFFSET :offset LIMIT :limit
            """,
            nativeQuery = true
    )
    fun getMostRecentArticlesForLang(langId: String, offset: Int, limit: Int): List<ArticleInListProjection>

    @Query(
            value =
            """
                SELECT
                art.id 
                FROM articles_langs art
                WHERE art.lang_id = :langId AND art.created_on_site IS NOT NULL
                ORDER BY art.created_on_site DESC
                OFFSET :offset LIMIT :limit
            """,
            nativeQuery = true
    )
    fun getMostRecentArticlesForLangIds(langId: String, offset: Int, limit: Int): List<Long>

    @Query(
            value = """
                select 
                art.id, 
                art.article_id as articleId,
                art.lang_id as langId,
                art.url_relative as urlRelative,
                art.title,
                art.rating,
                art.created_on_site as createdOnSite,
                art.has_iframe_tag as hasIframeTag 
                from articles_langs art
                where lang_id = :langId AND art.rating is not null
                order by rating desc 
                OFFSET :offset LIMIT :limit
            """,
            nativeQuery = true
    )
    fun getMostRatedArticlesForLang(langId: String, offset: Int, limit: Int): List<ArticleInListProjection>

    @Query(
            value = """
                select 
                art.id 
                from articles_langs art
                where lang_id = :langId AND art.rating is not null
                order by rating desc 
                OFFSET :offset LIMIT :limit
            """,
            nativeQuery = true
    )
    fun getMostRatedArticlesForLangIds(langId: String, offset: Int, limit: Int): List<Long>

    @Query(
            value = """
                select 
                art.id, 
                art.lang_id as langId,
                art.article_id as articleId,
                art.url_relative as urlRelative,
                art.title,
                art.rating,
                art.created_on_site as createdOnSite,
                art.has_iframe_tag as hasIframeTag 
                from articles_langs art
                join article_categories_to_lang__to__articles_to_lang art_cat on art.id = art_cat.article_to_lang_id
                where art.id in 
                    (select article_to_lang_id from article_categories_to_lang__to__articles_to_lang 
                        where article_category_to_lang_id = :articleCategoryToLangId order by order_in_category)
            """,
            nativeQuery = true
    )
    fun findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLangId: Long): List<ArticleInListProjection>

    @Query(
            value = """
                select 
                art.id, 
                art.lang_id as langId,
                art.article_id as articleId,
                art.url_relative as urlRelative,
                art.title,
                art.rating,
                art.created_on_site as createdOnSite,
                art.has_iframe_tag as hasIframeTag 
                from articles_langs art
                join article_categories_to_lang__to__articles_to_lang art_cat on art.id = art_cat.article_to_lang_id
                where art.id in :articleToLangIds
            """,
            nativeQuery = true
    )
    fun findAllByIds(articleToLangIds: List<Long>): List<ArticleInListProjection>

    @Query(
            """
                select 
                art.id, 
                art.lang_id as langId,
                art.article_id as articleId,
                art.url_relative as urlRelative,
                art.title,
                art.rating,
                art.created_on_site as createdOnSite,
                art.has_iframe_tag as hasIframeTag 
                from articles_langs art
                where art.has_iframe_tag='false'
                order by random() 
                limit 1
            """,
            nativeQuery = true
    )
    fun getRandomArticle(): ArticleInListProjection

    @Query(
            """
                select 
                art.id, 
                art.lang_id as langId,
                art.article_id as articleId,
                art.url_relative as urlRelative,
                art.title,
                art.rating,
                art.created_on_site as createdOnSite,
                art.has_iframe_tag as hasIframeTag 
                from articles_langs art
                where art.has_iframe_tag='false'
                and art.lang_id=:langId
                order by random() 
                limit 1
            """,
            nativeQuery = true
    )
    fun getRandomArticle(langId: String): ArticleInListProjection

    fun deleteByIdIn(ids: List<Long>)
}
