package ru.kuchanov.scpreaderapi.service.article.favorite

import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.favorite.FavoriteArticleByLang
import ru.kuchanov.scpreaderapi.model.dto.article.ReadOrFavoriteArticleProjection
import ru.kuchanov.scpreaderapi.model.dto.article.ReadOrFavoriteArticleToLangDto
import ru.kuchanov.scpreaderapi.repository.article.favorite.FavoriteArticlesForLangRepository
import ru.kuchanov.scpreaderapi.repository.article.tags.TagForLangRepository
import ru.kuchanov.scpreaderapi.service.article.image.ArticlesImagesService
import ru.kuchanov.scpreaderapi.service.article.type.ArticleAndArticleTypeService


@Service
class FavoriteArticleForLangServiceImpl constructor(
        val repository: FavoriteArticlesForLangRepository,
        val imagesService: ArticlesImagesService,
        val tagsForLangRepository: TagForLangRepository,
        val articleAndArticleTypeService: ArticleAndArticleTypeService
) : FavoriteArticleForLangService {

    override fun deleteById(id: Long) =
            repository.deleteById(id)

    override fun save(article: FavoriteArticleByLang): FavoriteArticleByLang =
            repository.save(article)

    override fun getFavoriteArticleForArticleIdLangIdAndUserId(articleToLangId: Long, userId: Long) =
            repository.findByArticleToLangIdAndUserId(articleToLangId, userId)

    override fun findByArticleToLangIdAndUserId(articleToLangId: Long, userId: Long): FavoriteArticleByLang? =
            repository.findByArticleToLangIdAndUserId(articleToLangId, userId)

    override fun findAllByUserIdAndLangId(
            userId: Long,
            langId: String?,
            offset: Int,
            limit: Int
    ): List<ReadOrFavoriteArticleToLangDto> =
            if (langId != null) {
                repository.findAllByUserIdAndLangId(userId, langId, offset, limit)
            } else {
                repository.findAllByUserId(userId, offset, limit)
            }
                    .map { it.toDto().withImages().withTags().withType() }


    fun ReadOrFavoriteArticleProjection.toDto() =
            ReadOrFavoriteArticleToLangDto(
                    id = id,
                    statusChangedDate = statusChangedDate,
                    articleToLangId = articleToLangId,
                    articleId = articleId,
                    langId = langId,
                    urlRelative = urlRelative,
                    rating = rating,
                    title = title,
                    createdOnSite = createdOnSite,
                    hasIframeTag = hasIframeTag
            )

    fun ReadOrFavoriteArticleToLangDto.withImages(): ReadOrFavoriteArticleToLangDto =
            this.apply { imageUrls = imagesService.findAllByArticleForLangId(articleForLangId = articleToLangId) }

    fun ReadOrFavoriteArticleToLangDto.withTags(): ReadOrFavoriteArticleToLangDto =
            this.apply {
                tagsForLang = tagsForLangRepository.getAllForLangIdAndArticleForLangIdAsDto(
                        langId = langId,
                        articleForLangId = articleToLangId
                )
            }

    fun ReadOrFavoriteArticleToLangDto.withType(): ReadOrFavoriteArticleToLangDto =
            this.apply { articleTypeToArticleDto = articleAndArticleTypeService.getByArticleIdAndLangIdAsDto(articleId, langId) }
}
