package ru.kuchanov.scpreaderapi.service.article.category

import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForArticle


interface ArticleCategoryForArticleService {
    fun findAllByArticleCategoryId(articleCategoryId: Long): List<ArticleCategoryForArticle>

    fun save(entity: ArticleCategoryForArticle): ArticleCategoryForArticle
    fun save(entities: List<ArticleCategoryForArticle>): List<ArticleCategoryForArticle>
}
