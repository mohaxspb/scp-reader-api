package ru.kuchanov.scpreaderapi.repository.article.text

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart
import ru.kuchanov.scpreaderapi.model.dto.article.TextPartProjection
import javax.transaction.Transactional

interface TextPartRepository : JpaRepository<TextPart, Long> {

    fun findAllByParentIdOrderByOrderInText(parentId: Long): List<TextPartProjection>

    fun findAllByArticleToLangIdAndParentIdNullOrderByOrderInText(articleToLangId: Long): List<TextPartProjection>

    @Query("""
        select * from article_to_lang_text_parts
        where article_to_lang_id in :articleToLangIds
        order by article_to_lang_id, parent_id nulls first, order_in_text;
    """, nativeQuery = true)
    fun findAllByArticleToLangIdAndParentIdNullOrderedCorrectlyBitch(
            articleToLangIds: List<Long>
    ): List<TextPart>

    @Transactional
    fun deleteByArticleToLangIdAndParentIdNull(articleToLangId: Long)
}
