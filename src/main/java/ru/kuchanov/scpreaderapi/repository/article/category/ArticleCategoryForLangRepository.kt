package ru.kuchanov.scpreaderapi.repository.article.category

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForLang

interface ArticleCategoryForLangRepository : JpaRepository<ArticleCategoryForLang, Long> {

    fun findByLangIdAndArticleCategoryId(langId: String, articleCategoryId: Long): ArticleCategoryForLang?

    fun findByLangIdAndSiteUrl(langId: String, siteUrl: String): ArticleCategoryForLang?
}
