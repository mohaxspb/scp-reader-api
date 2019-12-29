package ru.kuchanov.scpreaderapi.service.article.read

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticleByLang
import ru.kuchanov.scpreaderapi.model.dto.article.ReadOrFavoriteArticleProjection
import ru.kuchanov.scpreaderapi.model.dto.article.ReadOrFavoriteArticleToLangDto
import ru.kuchanov.scpreaderapi.repository.article.read.ReadArticlesForLangRepository
import ru.kuchanov.scpreaderapi.repository.article.tags.TagForLangRepository
import ru.kuchanov.scpreaderapi.service.article.image.ArticlesImagesService
import ru.kuchanov.scpreaderapi.service.article.type.ArticleAndArticleTypeService
import javax.transaction.Transactional


@Service
class ReadArticleForLangServiceImpl @Autowired constructor(
        val repository: ReadArticlesForLangRepository,
        val imagesService: ArticlesImagesService,
        val tagsForLangRepository: TagForLangRepository,
        val articleAndArticleTypeService: ArticleAndArticleTypeService
) : ReadArticleForLangService {

    override fun deleteById(id: Long) =
            repository.deleteById(id)

    @Transactional
    override fun save(article: ReadArticleByLang): ReadArticleByLang =
            repository.save(article)

    override fun findByArticleToLangIdAndUserId(articleToLangId: Long, userId: Long): ReadArticleByLang? =
            repository.findByArticleToLangIdAndUserId(articleToLangId, userId)

    override fun findAllByUserIdAndLangId(
            userId: Long,
            langId: String,
            offset: Int,
            limit: Int
    ): List<ReadOrFavoriteArticleToLangDto> = repository.findAllByUserIdAndLangId(userId, langId, offset, limit)
            .map { it.toDto().withImages().withTags().withType() }

    fun ReadOrFavoriteArticleProjection.toDto() =
            ReadOrFavoriteArticleToLangDto(
                    id = this.id,
                    readDate = this.readDate,
                    articleToLangId = this.articleToLangId,
                    articleId = this.articleId,
                    langId = this.langId,
                    urlRelative = this.urlRelative,
                    rating = this.rating,
                    title = this.title,
                    createdOnSite = this.createdOnSite,
                    hasIframeTag = this.hasIframeTag
            )

    fun ReadOrFavoriteArticleToLangDto.withImages(): ReadOrFavoriteArticleToLangDto =
            this.apply { imageUrls = imagesService.findAllByArticleForLangId(articleForLangId = id) }

    fun ReadOrFavoriteArticleToLangDto.withTags(): ReadOrFavoriteArticleToLangDto =
            this.apply {
                tagsForLang = tagsForLangRepository.getAllForLangIdAndArticleForLangIdAsDto(
                        langId = langId,
                        articleForLangId = id
                )
            }

    fun ReadOrFavoriteArticleToLangDto.withType(): ReadOrFavoriteArticleToLangDto =
            this.apply { articleTypeDto = articleAndArticleTypeService.getByArticleIdAndLangIdAsDto(articleId, langId) }
}
