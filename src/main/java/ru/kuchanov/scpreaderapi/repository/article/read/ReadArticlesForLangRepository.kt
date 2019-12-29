package ru.kuchanov.scpreaderapi.repository.article.read

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticleByLang
import ru.kuchanov.scpreaderapi.model.dto.article.ReadOrFavoriteArticleProjection

interface ReadArticlesForLangRepository : JpaRepository<ReadArticleByLang, Long> {

    fun findByArticleToLangIdAndUserId(articleToLangId: Long, userId: Long): ReadArticleByLang?

    @Query(
            """
                select 
                ra.id,
                ra.created as readDate,
                art.id as articleToLangId,
                art.article_id as articleId,
                art.lang_id as langId,
                art.url_relative as urlRelative,
                art.title,
                art.rating,
                art.created_on_site as createdOnSite,
                art.has_iframe_tag as hasIframeTag 
                from read__articles_to_lang__to__users ra
                join articles_langs art on art.id = ra.article_to_lang_id
                where ra.user_id = :userId and art.lang_id = :langId
                OFFSET :offset LIMIT :limit
            """,
            nativeQuery = true
    )
    fun findAllByUserIdAndLangId(
            userId: Long,
            langId: String,
            offset: Int,
            limit: Int
    ): List<ReadOrFavoriteArticleProjection>

    @Query(
            """
                select 
                ra.id,
                ra.created as readDate,
                art.id as articleToLangId,
                art.article_id as articleId,
                art.lang_id as langId,
                art.url_relative as urlRelative,
                art.title,
                art.rating,
                art.created_on_site as createdOnSite,
                art.has_iframe_tag as hasIframeTag 
                from read__articles_to_lang__to__users ra
                join articles_langs art on art.id = ra.article_to_lang_id
                where ra.user_id = :userId
                OFFSET :offset LIMIT :limit
            """,
            nativeQuery = true
    )
    fun findAllByUserId(
            userId: Long,
            offset: Int,
            limit: Int
    ): List<ReadOrFavoriteArticleProjection>
}
