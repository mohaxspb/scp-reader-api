package ru.kuchanov.scpreaderapi.service.article

import ru.kuchanov.scpreaderapi.bean.articles.image.ArticlesImages
import ru.kuchanov.scpreaderapi.model.dto.article.ArticlesImagesDto
import javax.transaction.Transactional

interface ArticlesImagesService {

    fun findAllByArticleForLangId(articleForLangId: Long): List<ArticlesImagesDto>

    @Transactional
    fun insert(articlesImages: ArticlesImages): ArticlesImages

    @Transactional
    fun insert(articlesImages: List<ArticlesImages>): List<ArticlesImages>
}
