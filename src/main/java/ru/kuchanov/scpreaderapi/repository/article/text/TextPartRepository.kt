package ru.kuchanov.scpreaderapi.repository.article.text

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart

interface TextPartRepository : JpaRepository<TextPart, Long> {

    fun findAllByParentId(parentId: Long): List<TextPart>

    fun findAllByArticleToLangId(articleToLangId: Long): List<TextPart>
}
