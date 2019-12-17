package ru.kuchanov.scpreaderapi.repository.article.text

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart
import javax.transaction.Transactional

interface TextPartRepository : JpaRepository<TextPart, Long> {

    fun findAllByParentId(parentId: Long): List<TextPart>

    fun findAllByArticleToLangId(articleToLangId: Long): List<TextPart>

    @Transactional
    fun deleteByArticleToLangId(articleToLangId: Long)
}
