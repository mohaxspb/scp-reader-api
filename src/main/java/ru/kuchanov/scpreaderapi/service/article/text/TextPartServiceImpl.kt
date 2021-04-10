package ru.kuchanov.scpreaderapi.service.article.text

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart
import ru.kuchanov.scpreaderapi.bean.articles.text.toDto
import ru.kuchanov.scpreaderapi.model.dto.article.TextPartDto
import ru.kuchanov.scpreaderapi.repository.article.text.TextPartRepository


@Service
class TextPartServiceImpl @Autowired constructor(
        val textPartRepository: TextPartRepository
) : TextPartService {

    override fun insert(data: TextPart): TextPart =
            textPartRepository.save(data)

    override fun insert(data: List<TextPart>): List<TextPart> =
            textPartRepository.saveAll(data)

    override fun findAllByArticleToLangIdV2(articleToLangId: Long): List<TextPartDto> =
            textPartsTreeFromFlattenedList(findAllByArticleToLangIds(listOf(articleToLangId)))

    override fun findAllByArticleToLangIds(articleToLangIds: List<Long>): List<TextPart> =
            textPartRepository.findAllByArticleToLangIdAndParentIdNullOrderedCorrectlyBitch(articleToLangIds)

    override fun deleteByArticleToLangId(articleToLangId: Long) =
            textPartRepository.deleteByArticleToLangIdAndParentIdNull(articleToLangId)

    override fun textPartsTreeFromFlattenedList(rawTextParts: List<TextPart>): List<TextPartDto> {
        val filledTextParts = mutableListOf<TextPartDto>()

        val map = rawTextParts.groupBy(
                keySelector = { it.parentId },
                valueTransform = { it.toDto() }
        )
        map.forEach { (parentId, textParts) ->
            if (parentId == null) {
                filledTextParts.addAll(textParts)
            } else {
                val textPartToFill = findTextPartToFill(filledTextParts, parentId)!!
                textPartToFill.innerTextParts = textParts
            }
        }

        return filledTextParts
    }

    private fun findTextPartToFill(textParts: List<TextPartDto>, parentId: Long): TextPartDto? {
        return textParts.firstOrNull { parentId == it.id }
                ?: findTextPartToFill(
                        textParts
                                .filter { it.innerTextParts.isNullOrEmpty().not() }
                                .map { it.innerTextParts!! }
                                .flatten(),
                        parentId
                )
    }
}
