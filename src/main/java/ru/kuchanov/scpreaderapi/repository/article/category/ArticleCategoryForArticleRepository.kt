package ru.kuchanov.scpreaderapi.repository.article.category

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForArticle

interface ArticleCategoryForArticleRepository : JpaRepository<ArticleCategoryForArticle, Long> {

    fun findAllByArticleCategoryId(articleCategoryId: Long): List<ArticleCategoryForArticle>
}
