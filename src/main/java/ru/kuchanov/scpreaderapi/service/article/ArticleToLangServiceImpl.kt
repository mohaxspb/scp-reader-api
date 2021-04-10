package ru.kuchanov.scpreaderapi.service.article

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleInListProjection
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleToLangDto
import ru.kuchanov.scpreaderapi.model.dto.article.toDto
import ru.kuchanov.scpreaderapi.repository.article.ArticlesForLangRepository
import ru.kuchanov.scpreaderapi.repository.article.tags.TagForLangRepository
import ru.kuchanov.scpreaderapi.service.article.image.ArticlesImagesService
import ru.kuchanov.scpreaderapi.service.article.text.TextPartService
import ru.kuchanov.scpreaderapi.service.article.type.ArticleAndArticleTypeService
import ru.kuchanov.scpreaderapi.utils.millisToMinutesAndSeconds


@Service
class ArticleToLangServiceImpl @Autowired constructor(
        val articlesForLangRepository: ArticlesForLangRepository,
        val imagesService: ArticlesImagesService,
        val tagsForLangRepository: TagForLangRepository,
        val articleAndArticleTypeService: ArticleAndArticleTypeService,
        val textPartService: TextPartService,
        private val log: Logger
) : ArticleForLangService {

    override fun save(articleForLang: ArticleForLang): ArticleForLang =
            articlesForLangRepository.save(articleForLang)

    override fun getOneByLangIdAndArticleId(articleId: Long, langId: String): ArticleForLang? =
            articlesForLangRepository.getOneByArticleIdAndLangId(articleId, langId)

    override fun getOneByLangIdAndArticleIdAsDto(articleId: Long, langId: String): ArticleToLangDto? =
            articlesForLangRepository
                    .getOneByArticleIdAndLangIdAsProjection(articleId, langId)
                    ?.toDto()
                    ?.withType()
                    ?.withImages()
                    ?.withTags()
                    ?.withTextPartsV2()

    override fun getOneByIdAsDto(id: Long): ArticleToLangDto? {
//        val startTime = System.currentTimeMillis()

        val article = articlesForLangRepository
                .getOneByIdAsProjection(id)
                ?.toDto()
                ?.withType()
                ?.withImages()
                ?.withTags()
                ?.withTextPartsV2()

//        val (minutes, seconds) = millisToMinutesAndSeconds(System.currentTimeMillis() - startTime)
//        log.error("findAllArticlesForLangByArticleCategoryToLangId END: " +
//                "(min:sec): $minutes:$seconds")

        return article
    }

    override fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String): ArticleForLang? =
            articlesForLangRepository.findByUrlRelativeAndLangId(urlRelative, langId)

    override fun getArticleForLangByUrlRelativeAndLangAsDto(urlRelative: String, langId: String): ArticleToLangDto? =
            articlesForLangRepository
                    .findByUrlRelativeAndLangIdAsProjection(urlRelative, langId)
                    ?.toDto()
                    ?.withType()
                    ?.withImages()
                    ?.withTags()
                    ?.withTextPartsV2()

    override fun getIdByUrlRelativeAndLangId(urlRelative: String, langId: String): Long? =
            articlesForLangRepository.getIdByUrlRelativeAndLangId(urlRelative, langId)

    override fun getCreatedArticleToLangsBetweenDates(startDate: String, endDate: String): List<ArticleToLangDto> =
            articlesForLangRepository.getCreatedArticlesBetweenDates(startDate, endDate)
                    .map { it.toDto() }

    override fun getMostRecentArticlesForLang(langId: String, offset: Int, limit: Int): List<ArticleToLangDto> =
            articlesForLangRepository
                    .getMostRecentArticlesForLang(langId, offset, limit)
                    .map { it.toDto().withImages().withTags().withType() }

    override fun getMostRecentArticlesForLangIds(langId: String, offset: Int, limit: Int): List<Long> =
            articlesForLangRepository
                    .getMostRecentArticlesForLangIds(langId, offset, limit)

    override fun getMostRatedArticlesForLang(langId: String, offset: Int, limit: Int): List<ArticleToLangDto> =
            articlesForLangRepository
                    .getMostRatedArticlesForLang(langId, offset, limit)
                    .map { it.toDto().withImages().withTags().withType() }

    override fun getMostRatedArticlesForLangIds(langId: String, offset: Int, limit: Int): List<Long> =
            articlesForLangRepository.getMostRatedArticlesForLangIds(langId, offset, limit)

    override fun findAllArticlesForLangByArticleCategoryToLangId(
            articleCategoryToLangId: Long
    ): List<ArticleToLangDto> {
        val startTime = System.currentTimeMillis()
        val articles = articlesForLangRepository
                .findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLangId)

        val articlesFilled = fillArticleToLangDtoByArticleToLangIds(articles)
        val (minutes, seconds) = millisToMinutesAndSeconds(System.currentTimeMillis() - startTime)
        log.error("findAllArticlesForLangByArticleCategoryToLangId END: " +
                "(min:sec): $minutes:$seconds")
        return articlesFilled
    }

    override fun findAllByIdsWithTextParts(articleToLangIds: List<Long>): List<ArticleToLangDto> {
//        val startTime = System.currentTimeMillis()
        val articles: List<ArticleInListProjection> = articlesForLangRepository
                .findAllByIds(articleToLangIds)

        val articlesFilled = fillArticleToLangDtoByArticleToLangIds(articles, true)
//        val (minutes, seconds) = millisToMinutesAndSeconds(System.currentTimeMillis() - startTime)
//        log.error("findAllArticlesForLangByArticleCategoryToLangId END: " +
//                "(min:sec): $minutes:$seconds")
        return articlesFilled
    }

    private fun fillArticleToLangDtoByArticleToLangIds(
            articleToLangs: List<ArticleInListProjection>,
            withTextParts: Boolean = false
    ): List<ArticleToLangDto> {
        if (articleToLangs.isEmpty()) {
            return listOf()
        }
        val articleToLangIds = articleToLangs.map { it.id }
//        log.error("articlesIds: ${articleToLangIds.size}")
        val articleIds = articleToLangs.map { it.articleId }

        val images = imagesService.findAllByArticleForLangIds(articleToLangIds)
                .groupBy(
                        keySelector = { it.articleForLangId },
                        valueTransform = { it.toDto() }
                )

        val tags = tagsForLangRepository.getAllForLangIdAndArticleForLangIds(articleToLangs[0].langId, articleToLangIds)
                .groupBy(
                        keySelector = { it.articleToLangId },
                        valueTransform = { it.toDto() }
                )
        val types = articleAndArticleTypeService
                .findByArticleIdInAndLangId(articleIds, articleToLangs[0].langId)

        val articlesToTextParts = if (withTextParts) {
            textPartService.findAllByArticleToLangIds(articleToLangIds).groupBy { it.articleToLangId }
        } else {
            null
        }

        return articleToLangs.map { article ->
            article.toDto().apply {
                imageUrls = images[id]
                tagDtos = tags[id]
                articleTypeToArticleDto = types.firstOrNull { articleId == it.articleId }?.toDto()
                if (withTextParts) {
                    this.textParts = articlesToTextParts?.get(id)?.let { textPartService.textPartsTreeFromFlattenedList(it) }
                }
            }
        }
    }

    override fun deleteByIds(ids: List<Long>) {
        articlesForLangRepository.deleteByIdIn(ids)
    }

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
                    .withTextPartsV2()

    fun ArticleInListProjection.toDto(): ArticleToLangDto =
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
                ).map { it.toDto() }
            }

    private fun ArticleToLangDto.withType(): ArticleToLangDto =
            this.apply {
                articleTypeToArticleDto =
                        articleAndArticleTypeService.getByArticleIdAndLangIdAsDto(articleId, langId)
            }

    private fun ArticleToLangDto.withTextPartsV2(): ArticleToLangDto {
        return if (!hasIframeTag) {
            this.apply { textParts = textPartService.findAllByArticleToLangIdV2(id) }
        } else {
            this
        }
    }
}
