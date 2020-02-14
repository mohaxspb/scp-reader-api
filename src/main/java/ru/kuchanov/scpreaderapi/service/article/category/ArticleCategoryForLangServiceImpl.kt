package ru.kuchanov.scpreaderapi.service.article.category

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleCategoryToLangProjection
import ru.kuchanov.scpreaderapi.repository.article.category.ArticleCategoryForLangRepository

@Service
class ArticleCategoryForLangServiceImpl @Autowired constructor(
        val repository: ArticleCategoryForLangRepository
) : ArticleCategoryForLangService {

    override fun findAllByLangId(langId: String): List<ArticleCategoryToLangProjection> =
            repository
                    .findAllByLangId(langId)
                    .toMutableList()
                    .apply {
                        add(repository.getRatedArticlesCategoryByLang(langId))
                        add(repository.getRecentArticlesCategoryByLang(langId))
                    }

    override fun findByLangIdAndArticleCategoryId(langId: String, articleCategoryId: Long): ArticleCategoryForLang? =
            repository.findByLangIdAndArticleCategoryId(langId, articleCategoryId)

    override fun findByLangIdAndSiteUrl(langId: String, siteUrl: String): ArticleCategoryForLang? =
            repository.findByLangIdAndSiteUrl(langId, siteUrl)
}
