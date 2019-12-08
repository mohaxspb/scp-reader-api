package ru.kuchanov.scpreaderapi.service.article.category

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForLangToArticleForLang
import ru.kuchanov.scpreaderapi.repository.article.category.ArticleCategoryForArticleRepository
import javax.transaction.Transactional

@Service
class ArticleCategoryForArticleServiceImpl @Autowired constructor(
        val repository: ArticleCategoryForArticleRepository
) : ArticleCategoryForArticleService {

    override fun findAllByArticleCategoryToLangId(articleCategoryToLangId: Long): List<ArticleCategoryForLangToArticleForLang> =
            repository.findAllByArticleCategoryToLangId(articleCategoryToLangId)

    override fun save(entity: ArticleCategoryForLangToArticleForLang): ArticleCategoryForLangToArticleForLang =
            repository.save(entity)

    @Transactional
    override fun save(entities: List<ArticleCategoryForLangToArticleForLang>): List<ArticleCategoryForLangToArticleForLang> =
            repository.saveAll(entities)

    override fun updateCategoryForLangToArticleForLang(
            articleCategoryToLangId: Long,
            articlesForCategory: List<ArticleCategoryForLangToArticleForLang>
    ): List<ArticleCategoryForLangToArticleForLang> {
        repository.deleteAllByArticleCategoryToLangId(articleCategoryToLangId)
        return save(articlesForCategory)
    }
}
