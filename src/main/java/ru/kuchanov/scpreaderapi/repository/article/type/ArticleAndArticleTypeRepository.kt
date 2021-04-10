package ru.kuchanov.scpreaderapi.repository.article.type

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.types.ArticlesAndArticleTypes
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleTypeToArticleProjection

interface ArticleAndArticleTypeRepository : JpaRepository<ArticlesAndArticleTypes, Long> {

    fun findByArticleId(articleId: Long): ArticlesAndArticleTypes?

    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    @Query("""
            select
                   aat.id,
                   aat.article_type_id as articleTypeId,
                   aat.article_id      as articleId,
                   attl.title          as titleForLang,
                   at.enum_value       as enumValue
            from article__to__article_type aat
                     join article_types at on at.id = aat.article_type_id
                     join article_types__to__langs attl on at.id = attl.article_type_id
            where aat.article_id in :articleIds
              and attl.lang_id = :langId
            order by aat.article_id
    """, nativeQuery = true)
    fun findByArticleIdInAndLangId(articleIds: List<Long>, langId: String): List<ArticleTypeToArticleProjection>
}
