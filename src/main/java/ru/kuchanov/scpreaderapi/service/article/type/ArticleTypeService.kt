package ru.kuchanov.scpreaderapi.service.article.type

import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.types.ArticleType

interface ArticleTypeService {
    fun getByEnumValue(articleTypeEnum: ScpReaderConstants.ArticleTypeEnum): ArticleType

    fun getEnumValueById(articleTypeId: Long): ScpReaderConstants.ArticleTypeEnum?
}
