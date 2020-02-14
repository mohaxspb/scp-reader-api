package ru.kuchanov.scpreaderapi.service.article.category

import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleCategoryToLangProjection


interface ArticleCategoryForLangService {
    fun findAllByLangId(langId: String): List<ArticleCategoryToLangProjection>

    fun findByLangIdAndArticleCategoryId(langId: String, articleCategoryId: Long): ArticleCategoryForLang?

    fun findByLangIdAndSiteUrl(langId: String, siteUrl: String): ArticleCategoryForLang?
}
