package ru.kuchanov.scpreaderapi.service.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInList
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInListProjection
import ru.kuchanov.scpreaderapi.repository.article.ArticlesForLangRepository
import ru.kuchanov.scpreaderapi.repository.article.ArticlesImagesRepository
import ru.kuchanov.scpreaderapi.repository.article.tags.TagForLangRepository


@Service
class ArticleForLangServiceImpl @Autowired constructor(
        val articlesForLangRepository: ArticlesForLangRepository,
        val imagesRepository: ArticlesImagesRepository,
        val tagsForLangRepository: TagForLangRepository
) : ArticleForLangService {

    override fun insert(articleForLang: ArticleForLang): ArticleForLang =
            articlesForLangRepository.save(articleForLang)

    override fun insert(articleForLang: List<ArticleForLang>): List<ArticleForLang> =
            articlesForLangRepository.saveAll(articleForLang)

    override fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String) =
            articlesForLangRepository.findByUrlRelativeAndLangId(urlRelative, langId)

    override fun getIdByUrlRelativeAndLangId(urlRelative: String, langId: String) =
            articlesForLangRepository.getIdByUrlRelativeAndLangId(urlRelative, langId)

    override fun getMostRecentArticlesForLang(langId: String, offset: Int, limit: Int) =
            articlesForLangRepository
                    .getMostRecentArticlesForLang(langId, offset, limit)
                    .map { it.withImages().withTags() }

    override fun findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLangId: Long) =
            articlesForLangRepository
                    .findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLangId)
                    .map { it.toArticleInList().withImages().withTags() }

    override fun getOneByLangAndArticleId(articleId: Long, langId: String) =
            articlesForLangRepository.getOneByArticleIdAndLangId(articleId, langId)

    fun ArticleInListProjection.toArticleInList() =
            ArticleInList(
                    id = this.id,
                    articleId = this.articleId,
                    langId = this.langId,
                    urlRelative = this.urlRelative,
                    rating = this.rating,
                    title = this.title
            )

    fun ArticleInList.withImages(): ArticleInList =
            this.apply { imageUrls = imagesRepository.findAllByArticleForLangId(articleForLangId = id) }

    fun ArticleInList.withTags(): ArticleInList =
            this.apply {
                tagsForLang = tagsForLangRepository.getAllForLangIdAndArticleForLangIdAsDto(
                        langId = langId,
                        articleForLangId = id
                )
            }
}
