package ru.kuchanov.scpreaderapi.repository.article

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.ArticlesImages
import ru.kuchanov.scpreaderapi.model.dto.article.ArticlesImagesDto

interface ArticlesImagesRepository : JpaRepository<ArticlesImages, Long> {

    fun findAllByArticleForLangId(articleForLangId: Long): List<ArticlesImagesDto>
}