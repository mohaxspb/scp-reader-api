package ru.kuchanov.scpreaderapi.service.article.type

import ru.kuchanov.scpreaderapi.bean.articles.types.ArticlesAndArticleTypes
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleTypeDto

interface ArticleAndArticleTypeService {
    fun save(articlesAndArticleTypes: ArticlesAndArticleTypes): ArticlesAndArticleTypes

    fun getByArticleId(articleId: Long): ArticlesAndArticleTypes?

    fun getByArticleIdAndLangIdAsDto(articleId: Long, langId: String): ArticleTypeDto?
}
