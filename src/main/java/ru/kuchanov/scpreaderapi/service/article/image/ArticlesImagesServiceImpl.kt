package ru.kuchanov.scpreaderapi.service.article.image

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.image.ArticlesImages
import ru.kuchanov.scpreaderapi.model.dto.article.ArticlesImagesDto
import ru.kuchanov.scpreaderapi.model.dto.article.ArticlesImagesProjection
import ru.kuchanov.scpreaderapi.repository.article.image.ArticlesImagesRepository


@Service
class ArticlesImagesServiceImpl @Autowired constructor(
        private val repository: ArticlesImagesRepository
) : ArticlesImagesService {

    override fun findAllByArticleForLangId(articleForLangId: Long): List<ArticlesImagesDto> =
            repository.findAllByArticleForLangId(articleForLangId)

    override fun findAllByArticleForLangIds(articleForLangIds: List<Long>): List<ArticlesImagesProjection> =
            repository.findAllByArticleForLangIdIn(articleForLangIds)

    override fun save(articlesImages: List<ArticlesImages>): List<ArticlesImages> =
            repository.saveAll(articlesImages)

    override fun deleteAllByArticleForLangId(articleForLangId: Long) {
        repository.deleteAllByArticleForLangId(articleForLangId)
    }
}
