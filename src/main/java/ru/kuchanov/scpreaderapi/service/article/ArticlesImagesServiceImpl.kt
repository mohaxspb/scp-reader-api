package ru.kuchanov.scpreaderapi.service.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.image.ArticlesImages
import ru.kuchanov.scpreaderapi.model.dto.article.ArticlesImagesDto
import ru.kuchanov.scpreaderapi.repository.article.ArticlesImagesRepository


@Service
class ArticlesImagesServiceImpl @Autowired constructor(
        val repository: ArticlesImagesRepository
) : ArticlesImagesService {

    override fun findAllByArticleForLangId(articleForLangId: Long): List<ArticlesImagesDto> =
            repository.findAllByArticleForLangId(articleForLangId)

    override fun insert(articlesImages: ArticlesImages): ArticlesImages =
            repository.save(articlesImages)

    override fun insert(articlesImages: List<ArticlesImages>): List<ArticlesImages> =
            repository.saveAll(articlesImages)
}
