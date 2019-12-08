package ru.kuchanov.scpreaderapi.service.article

import ru.kuchanov.scpreaderapi.bean.articles.ArticlesImages
import javax.transaction.Transactional

interface ArticlesImagesService {

    fun findAll(): List<ArticlesImages>

    fun update(articlesImages: ArticlesImages): ArticlesImages

    @Transactional
    fun insert(articlesImages: ArticlesImages): ArticlesImages

    fun insert(articlesImages: List<ArticlesImages>): List<ArticlesImages>

    //todo
}