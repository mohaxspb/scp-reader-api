package ru.kuchanov.scpreaderapi.service.article.text

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart
import ru.kuchanov.scpreaderapi.model.dto.article.TextPartDto
import ru.kuchanov.scpreaderapi.model.dto.article.TextPartProjection
import ru.kuchanov.scpreaderapi.repository.article.text.TextPartRepository


@Service
class TextPartServiceImpl @Autowired constructor(
        val textPartRepository: TextPartRepository
) : TextPartService {

    override fun insert(data: TextPart): TextPart =
            textPartRepository.save(data)

    override fun insert(data: List<TextPart>): List<TextPart> =
            textPartRepository.saveAll(data)

    override fun findAllByParentId(parentId: Long): List<TextPartDto> =
            textPartRepository
                    .findAllByParentIdOrderByOrderInText(parentId)
                    .map { it.toDto() }

    override fun findAllByArticleToLangId(articleToLangId: Long): List<TextPartDto> {
        val topLevelTextParts = textPartRepository
                .findAllByArticleToLangIdAndParentIdNullOrderByOrderInText(articleToLangId)
                .map { it.toDto() }

        topLevelTextParts.forEach { fillInnerTextParts(it) }

        return topLevelTextParts
    }

    override fun deleteByArticleToLongId(articleToLangId: Long) =
            textPartRepository.deleteByArticleToLangIdAndParentIdNull(articleToLangId)

    private fun fillInnerTextParts(textPart: TextPartDto) {
        textPart.innerTextParts = findAllByParentId(textPart.id)
        textPart.innerTextParts!!.forEach { fillInnerTextParts(it) }
    }

    private fun TextPartProjection.toDto(): TextPartDto =
            TextPartDto(id, data, type)
}
