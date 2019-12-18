package ru.kuchanov.scpreaderapi.service.article.text

import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart
import ru.kuchanov.scpreaderapi.model.dto.article.TextPartDto
import javax.transaction.Transactional

interface TextPartService {

    @Transactional
    fun insert(data: TextPart): TextPart

    @Transactional
    fun insert(data: List<TextPart>): List<TextPart>

    fun findAllByParentId(parentId: Long): List<TextPartDto>

    fun findAllByArticleToLangId(articleToLangId: Long): List<TextPartDto>

    @Transactional
    fun deleteByArticleToLongId(articleToLangId: Long)
}
