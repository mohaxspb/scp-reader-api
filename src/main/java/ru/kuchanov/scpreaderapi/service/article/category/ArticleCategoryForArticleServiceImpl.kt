package ru.kuchanov.scpreaderapi.service.article.category

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForArticle
import ru.kuchanov.scpreaderapi.repository.article.category.ArticleCategoryForArticleRepository

@Service
class ArticleCategoryForArticleServiceImpl @Autowired constructor(
        val repository: ArticleCategoryForArticleRepository
) : ArticleCategoryForArticleService {

    override fun findAllByArticleCategoryId(articleCategoryId: Long): List<ArticleCategoryForArticle> =
            repository.findAllByArticleCategoryId(articleCategoryId)

    override fun save(entity: ArticleCategoryForArticle): ArticleCategoryForArticle =
            repository.save(entity)

    override fun save(entities: List<ArticleCategoryForArticle>): List<ArticleCategoryForArticle> =
            repository.saveAll(entities)
}
