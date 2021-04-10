package ru.kuchanov.scpreaderapi.service.article.text

import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart
import ru.kuchanov.scpreaderapi.model.dto.article.TextPartDto
import javax.transaction.Transactional

interface TextPartService {

    @Transactional
    fun insert(data: TextPart): TextPart

    @Transactional
    fun insert(data: List<TextPart>): List<TextPart>

    fun findAllByArticleToLangIdV2(articleToLangId: Long): List<TextPartDto>

    fun findAllByArticleToLangIds(articleToLangIds: List<Long>): List<TextPart>

    fun textPartsTreeFromFlattenedList(rawTextParts: List<TextPart>): List<TextPartDto>

    @Transactional
    fun deleteByArticleToLangId(articleToLangId: Long)
}
