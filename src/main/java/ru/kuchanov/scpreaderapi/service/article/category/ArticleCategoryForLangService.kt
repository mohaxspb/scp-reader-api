package ru.kuchanov.scpreaderapi.service.article.category

import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForLang


interface ArticleCategoryForLangService {
    fun findByLangIdAndArticleCategoryId(langId: String, articleCategoryId: Long): ArticleCategoryForLang?
}
