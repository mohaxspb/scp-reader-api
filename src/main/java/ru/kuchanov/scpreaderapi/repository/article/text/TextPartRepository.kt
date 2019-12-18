package ru.kuchanov.scpreaderapi.repository.article.text

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart
import ru.kuchanov.scpreaderapi.model.dto.article.TextPartProjection
import javax.transaction.Transactional

interface TextPartRepository : JpaRepository<TextPart, Long> {

    fun findAllByParentIdOrderByOrderInText(parentId: Long): List<TextPartProjection>

    fun findAllByArticleToLangIdAndParentIdNullOrderByOrderInText(articleToLangId: Long): List<TextPartProjection>

    @Transactional
    fun deleteByArticleToLangIdAndParentIdNull(articleToLangId: Long)
}
