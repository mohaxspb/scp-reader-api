package ru.kuchanov.scpreaderapi.service.article.type

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.repository.article.type.ArticleTypeRepository

@Service
class ArticleTypeServiceImpl @Autowired constructor(
        val articleTypeRepository: ArticleTypeRepository
) : ArticleTypeService {
    override fun getByEnumValue(articleTypeEnum: ScpReaderConstants.ArticleTypeEnum) =
            articleTypeRepository.findByEnumValue(articleTypeEnum)

    override fun getEnumValueById(articleTypeId: Long): ScpReaderConstants.ArticleTypeEnum? =
            articleTypeRepository.findByIdOrNull(articleTypeId)?.enumValue
}
