package ru.kuchanov.scpreaderapi.repository.article.category

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategory

interface ArticleCategoryRepository : JpaRepository<ArticleCategory, Long> {

    fun findByDefaultTitle(defaultTitle: String): ArticleCategory?
}
