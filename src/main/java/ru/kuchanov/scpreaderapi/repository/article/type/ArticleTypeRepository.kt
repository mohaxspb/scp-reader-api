package ru.kuchanov.scpreaderapi.repository.article.type

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.types.ArticleType

interface ArticleTypeRepository : JpaRepository<ArticleType, Long> {

    fun findByEnumValue(enumValue: ScpReaderConstants.ArticleTypeEnum): ArticleType
}
