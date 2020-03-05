package ru.kuchanov.scpreaderapi.service.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInListProjection
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleToLangDto
import ru.kuchanov.scpreaderapi.repository.article.ArticlesForLangRepository
import ru.kuchanov.scpreaderapi.repository.article.tags.TagForLangRepository
import ru.kuchanov.scpreaderapi.service.article.image.ArticlesImagesService
import ru.kuchanov.scpreaderapi.service.article.text.TextPartService
import ru.kuchanov.scpreaderapi.service.article.type.ArticleAndArticleTypeService


@Service
class ArticleToLangServiceImpl @Autowired constructor(
        val articlesForLangRepository: ArticlesForLangRepository,
        val imagesService: ArticlesImagesService,
        val tagsForLangRepository: TagForLangRepository,
        val articleAndArticleTypeService: ArticleAndArticleTypeService,
        val textPartService: TextPartService
) : ArticleForLangService {

    override fun save(articleForLang: ArticleForLang): ArticleForLang =
            articlesForLangRepository.save(articleForLang)

    override fun getOneByLangIdAndArticleId(articleId: Long, langId: String) =
            articlesForLangRepository.getOneByArticleIdAndLangId(articleId, langId)

    override fun getOneByLangIdAndArticleIdAsDto(articleId: Long, langId: String) =
            articlesForLangRepository
                    .getOneByArticleIdAndLangIdAsProjection(articleId, langId)
                    ?.toDto()
                    ?.withType()
                    ?.withImages()
                    ?.withTags()
                    ?.withTextParts()

    override fun getOneByIdAsDto(id: Long): ArticleToLangDto? =
            articlesForLangRepository
                    .getOneByIdAsProjection(id)
                    ?.toDto()
                    ?.withType()
                    ?.withImages()
                    ?.withTags()
                    ?.withTextParts()

    override fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String) =
            articlesForLangRepository.findByUrlRelativeAndLangId(urlRelative, langId)

    override fun getArticleForLangByUrlRelativeAndLangAsDto(urlRelative: String, langId: String) =
            articlesForLangRepository
                    .findByUrlRelativeAndLangIdAsProjection(urlRelative, langId)
                    ?.toDto()
                    ?.withType()
                    ?.withImages()
                    ?.withTags()
                    ?.withTextParts()

    override fun getIdByUrlRelativeAndLangId(urlRelative: String, langId: String) =
            articlesForLangRepository.getIdByUrlRelativeAndLangId(urlRelative, langId)

    override fun getMostRecentArticlesForLang(langId: String, offset: Int, limit: Int) =
            articlesForLangRepository
                    .getMostRecentArticlesForLang(langId, offset, limit)
                    .map { it.toDto().withImages().withTags().withType() }

    override fun getMostRatedArticlesForLang(langId: String, offset: Int, limit: Int): List<ArticleToLangDto> =
            articlesForLangRepository
                    .getMostRatedArticlesForLang(langId, offset, limit)
                    .map { it.toDto().withImages().withTags().withType() }

    override fun findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLangId: Long) =
            articlesForLangRepository
                    .findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLangId)
                    .map { it.toDto().withImages().withTags().withType() }

    override fun deleteByIds(ids: List<Long>) =
            ids.forEach { articlesForLangRepository.deleteById(it) }

    override fun findIdsByArticleIds(articleIds: List<Long>): List<Long> =
            articlesForLangRepository.getIdsByArticleIds(articleIds)

    override fun getRandomArticle(langId: String?): ArticleToLangDto =
            if (langId == null) {
                articlesForLangRepository.getRandomArticle()
            } else {
                articlesForLangRepository.getRandomArticle(langId)
            }
                    .toDto()
                    .withImages()
                    .withTags()
                    .withType()
                    .withTextParts()

    fun ArticleInListProjection.toDto() =
            ArticleToLangDto(
                    id = this.id,
                    articleId = this.articleId,
                    langId = this.langId,
                    urlRelative = this.urlRelative,
                    rating = this.rating,
                    title = this.title,
                    createdOnSite = this.createdOnSite,
                    hasIframeTag = this.hasIframeTag
            )

    private fun ArticleToLangDto.withImages(): ArticleToLangDto =
            this.apply { imageUrls = imagesService.findAllByArticleForLangId(articleForLangId = id) }

    private fun ArticleToLangDto.withTags(): ArticleToLangDto =
            this.apply {
                tagDtos = tagsForLangRepository.getAllForLangIdAndArticleForLangIdAsDto(
                        langId = langId,
                        articleForLangId = id
                )
            }

    private fun ArticleToLangDto.withType(): ArticleToLangDto =
            this.apply { articleTypeToArticleDto = articleAndArticleTypeService.getByArticleIdAndLangIdAsDto(articleId, langId) }

    private fun ArticleToLangDto.withTextParts(): ArticleToLangDto {
        return if (!hasIframeTag) {
            this.apply { textParts = textPartService.findAllByArticleToLangId(id) }
        } else {
            this
        }
    }
}
