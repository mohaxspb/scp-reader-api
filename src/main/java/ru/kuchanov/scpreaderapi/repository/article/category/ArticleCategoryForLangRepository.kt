package ru.kuchanov.scpreaderapi.repository.article.category

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleCategoryToLangProjection

interface ArticleCategoryForLangRepository : JpaRepository<ArticleCategoryForLang, Long> {

    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    @Query(
            value = """
            select 
                acl.id, 
                ac.id as articleCategoryId, 
                ac.default_title as defaultTitle, 
                acl.title, 
                COALESCE(aclal.articlesCount, 0) as articlesCount 
            from article_categories__to__langs acl 
                     join article_categories ac on acl.article_category_id = ac.id 
                     left outer join (
                         select article_category_to_lang_id, count(article_to_lang_id) as articlesCount 
                         from article_categories_to_lang__to__articles_to_lang aclal 
                         group by aclal.article_category_to_lang_id
                ) aclal on acl.id = aclal.article_category_to_lang_id 
            where acl.lang_id = :langId 
            group by acl.id, ac.id, aclal.articlesCount, aclal.article_category_to_lang_id
    """,
            nativeQuery = true
    )
    fun findAllByLangId(langId: String): List<ArticleCategoryToLangProjection>

    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    @Query(
            value = """
            select 
                1000000 as id, 
                1000000 as articleCategoryId, 
                'Most rated' as defaultTitle, 
                'Most rated' as title, 
                COALESCE(count(al.lang_id), 0) as articlesCount 
                from articles_langs al 
                where al.lang_id = :langId and al.rating IS NOT NULL
            """,
            nativeQuery = true
    )
    fun getRatedArticlesCategoryByLang(langId: String): ArticleCategoryToLangProjection

    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    @Query(
            value = """
            select 
                2000000 as id, 
                2000000 as articleCategoryId, 
                'Most recent' as defaultTitle, 
                'Most recent' as title, 
                COALESCE(count(al.lang_id), 0) as articlesCount 
                from articles_langs al 
                where al.lang_id = :langId and al.created_on_site IS NOT NULL
            """,
            nativeQuery = true
    )
    fun getRecentArticlesCategoryByLang(langId: String): ArticleCategoryToLangProjection

    fun findByLangIdAndArticleCategoryId(langId: String, articleCategoryId: Long): ArticleCategoryForLang?

    fun findByLangIdAndSiteUrl(langId: String, siteUrl: String): ArticleCategoryForLang?
}
