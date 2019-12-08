package ru.kuchanov.scpreaderapi.repository.article.category

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForLangToArticleForLang
import javax.transaction.Transactional

interface ArticleCategoryForArticleRepository : JpaRepository<ArticleCategoryForLangToArticleForLang, Long> {

    fun findAllByArticleCategoryToLangId(articleCategoryToLangId: Long): List<ArticleCategoryForLangToArticleForLang>

    @Transactional
    fun deleteAllByArticleCategoryToLangId(articleCategoryToLangId: Long)
}
