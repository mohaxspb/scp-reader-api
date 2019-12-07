package ru.kuchanov.scpreaderapi.service.article.category

import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForLangToArticleForLang


interface ArticleCategoryForArticleService {
    fun findAllByArticleCategoryToLangId(articleCategoryToLangId: Long): List<ArticleCategoryForLangToArticleForLang>

    fun save(entity: ArticleCategoryForLangToArticleForLang): ArticleCategoryForLangToArticleForLang
    fun save(entities: List<ArticleCategoryForLangToArticleForLang>): List<ArticleCategoryForLangToArticleForLang>
}
