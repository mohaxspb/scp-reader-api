package ru.kuchanov.scpreaderapi.repository.article.category

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForLangToArticleForLang

interface ArticleCategoryForArticleRepository : JpaRepository<ArticleCategoryForLangToArticleForLang, Long> {

    fun findAllByArticleCategoryToLangId(articleCategoryId: Long): List<ArticleCategoryForLangToArticleForLang>
}
