package ru.kuchanov.scpreaderapi.service.article.type

import ru.kuchanov.scpreaderapi.bean.articles.types.ArticlesAndArticleTypes

interface ArticleAndArticleTypeService {
    fun save(articlesAndArticleTypes: ArticlesAndArticleTypes): ArticlesAndArticleTypes
    fun getByArticleId(articleId: Long): ArticlesAndArticleTypes?
}
