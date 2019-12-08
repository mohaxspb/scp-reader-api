package ru.kuchanov.scpreaderapi.service.article.category

import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategory


interface ArticleCategoryService {
    fun getByDefaultTitle(defaultTitle: String): ArticleCategory?
    fun getById(categoryId: Long): ArticleCategory?
}
