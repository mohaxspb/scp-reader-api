package ru.kuchanov.scpreaderapi.service.article.text

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart
import ru.kuchanov.scpreaderapi.repository.article.text.TextPartRepository


@Service
class TextPartServiceImpl @Autowired constructor(
        val textPartRepository: TextPartRepository
) : TextPartService {

    override fun insert(data: TextPart): TextPart =
            textPartRepository.save(data)

    override fun insert(data: List<TextPart>): List<TextPart> =
            textPartRepository.saveAll(data)

    override fun findAllByParentId(parentId: Long): List<TextPart> =
            textPartRepository.findAllByParentId(parentId)

    override fun findAllByArticleToLangId(articleToLangId: Long): List<TextPart> {
        val allTextParts = textPartRepository.findAllByArticleToLangId(articleToLangId)

        //todo create multiDimensional structure
        //todo use DTO

        return allTextParts
    }

    override fun deleteByArticleToLongId(articleToLangId: Long) =
            textPartRepository.deleteByArticleToLangId(articleToLangId)
}
