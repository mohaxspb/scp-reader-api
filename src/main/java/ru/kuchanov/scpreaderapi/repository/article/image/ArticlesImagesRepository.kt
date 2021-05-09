package ru.kuchanov.scpreaderapi.repository.article.image

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.image.ArticlesImages
import ru.kuchanov.scpreaderapi.model.dto.article.ArticlesImagesDto
import ru.kuchanov.scpreaderapi.model.dto.article.ArticlesImagesProjection
import javax.transaction.Transactional

interface ArticlesImagesRepository : JpaRepository<ArticlesImages, Long> {

    fun findAllByArticleForLangId(articleForLangId: Long): List<ArticlesImagesDto>

    fun findByArticleForLangIdAndUrl(articleForLangId: Long, url: String): ArticlesImages?

    @Transactional
    fun deleteAllByArticleForLangId(articleForLangId: Long)

    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    fun findAllByArticleForLangIdIn(articleForLangIds: List<Long>): List<ArticlesImagesProjection>
}