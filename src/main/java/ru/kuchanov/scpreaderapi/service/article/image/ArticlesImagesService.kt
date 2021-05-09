package ru.kuchanov.scpreaderapi.service.article.image

import ru.kuchanov.scpreaderapi.bean.articles.image.ArticlesImages
import ru.kuchanov.scpreaderapi.model.dto.article.ArticlesImagesDto
import ru.kuchanov.scpreaderapi.model.dto.article.ArticlesImagesProjection
import javax.transaction.Transactional

interface ArticlesImagesService {

    fun findAllByArticleForLangId(articleForLangId: Long): List<ArticlesImagesDto>

    fun findAllByArticleForLangIds(articleForLangIds: List<Long>): List<ArticlesImagesProjection>

    @Transactional
    fun save(articlesImages: List<ArticlesImages>): List<ArticlesImages>

    @Transactional
    fun deleteAllByArticleForLangId(articleForLangId: Long)
}
