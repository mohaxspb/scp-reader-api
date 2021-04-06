package ru.kuchanov.scpreaderapi.service.article.type

import ru.kuchanov.scpreaderapi.bean.articles.types.ArticlesAndArticleTypes
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleTypeToArticleDto
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleTypeToArticleProjection

interface ArticleAndArticleTypeService {
    fun save(articlesAndArticleTypes: ArticlesAndArticleTypes): ArticlesAndArticleTypes

    fun getByArticleId(articleId: Long): ArticlesAndArticleTypes?

    fun getByArticleIdAndLangIdAsDto(articleId: Long, langId: String): ArticleTypeToArticleDto?

    fun findByArticleIdInAndLangId(articleIds: List<Long>, langId: String): List<ArticleTypeToArticleProjection>
}
