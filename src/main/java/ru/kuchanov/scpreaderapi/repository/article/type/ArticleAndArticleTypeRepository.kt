package ru.kuchanov.scpreaderapi.repository.article.type

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.types.ArticlesAndArticleTypes

interface ArticleAndArticleTypeRepository : JpaRepository<ArticlesAndArticleTypes, Long> {
    fun findByArticleId(articleId: Long): ArticlesAndArticleTypes?
}
