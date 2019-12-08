package ru.kuchanov.scpreaderapi.service.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticlesImages
import ru.kuchanov.scpreaderapi.repository.article.ArticlesImagesRepository


@Suppress("unused")
@Service
class ArticlesImagesServiceImpl : ArticlesImagesService {

    @Autowired
    private lateinit var repository: ArticlesImagesRepository

    override fun findAll(): MutableList<ArticlesImages> = repository.findAll()

    override fun update(articlesImages: ArticlesImages): ArticlesImages = repository.save(articlesImages)

    override fun insert(articlesImages: ArticlesImages): ArticlesImages = repository.save(articlesImages)

    override fun insert(articlesImages: List<ArticlesImages>): List<ArticlesImages> = repository.saveAll(articlesImages)

    //todo
}