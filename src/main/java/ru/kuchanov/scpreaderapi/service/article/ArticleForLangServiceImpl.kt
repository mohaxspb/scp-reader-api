package ru.kuchanov.scpreaderapi.service.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleForLangDto
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInListProjection
import ru.kuchanov.scpreaderapi.repository.article.ArticlesForLangRepository
import ru.kuchanov.scpreaderapi.repository.article.tags.TagForLangRepository
import ru.kuchanov.scpreaderapi.service.article.image.ArticlesImagesService
import ru.kuchanov.scpreaderapi.service.article.text.TextPartService
import ru.kuchanov.scpreaderapi.service.article.type.ArticleAndArticleTypeService


@Service
class ArticleForLangServiceImpl @Autowired constructor(
        val articlesForLangRepository: ArticlesForLangRepository,
        val imagesService: ArticlesImagesService,
        val tagsForLangRepository: TagForLangRepository,
        val articleAndArticleTypeService: ArticleAndArticleTypeService,
        val textPartService: TextPartService
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
                    .map { it.toDto().withImages().withTags().withType() }

    override fun getMostRatedArticlesForLang(langId: String, offset: Int, limit: Int): List<ArticleForLangDto> =
            articlesForLangRepository
                    .getMostRatedArticlesForLang(langId, offset, limit)
                    .map { it.toDto().withImages().withTags().withType() }

    override fun findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLangId: Long) =
            articlesForLangRepository
                    .findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLangId)
                    .map { it.toDto().withImages().withTags().withType() }

    override fun getOneByLangAndArticleId(articleId: Long, langId: String) =
            articlesForLangRepository.getOneByArticleIdAndLangId(articleId, langId)

    override fun getOneByLangIdAndArticleIdAsDto(articleId: Long, langId: String) =
            articlesForLangRepository
                    .getOneByArticleIdAndLangIdAsProjection(articleId, langId)
                    ?.toDto()
                    ?.withType()
                    ?.withImages()
                    ?.withTags()
                    ?.withTextParts()

    fun ArticleInListProjection.toDto() =
            ArticleForLangDto(
                    id = this.id,
                    articleId = this.articleId,
                    langId = this.langId,
                    urlRelative = this.urlRelative,
                    rating = this.rating,
                    title = this.title,
                    createdOnSite = this.createdOnSite
            )

    fun ArticleForLangDto.withImages(): ArticleForLangDto =
            this.apply { imageUrls = imagesService.findAllByArticleForLangId(articleForLangId = id) }

    fun ArticleForLangDto.withTags(): ArticleForLangDto =
            this.apply {
                tagsForLang = tagsForLangRepository.getAllForLangIdAndArticleForLangIdAsDto(
                        langId = langId,
                        articleForLangId = id
                )
            }

    fun ArticleForLangDto.withType(): ArticleForLangDto =
            this.apply { articleTypeDto = articleAndArticleTypeService.getByArticleIdAndLangIdAsDto(articleId, langId) }

    fun ArticleForLangDto.withTextParts(): ArticleForLangDto =
            this.apply { textParts = textPartService.findAllByArticleToLangId(id) }
}
