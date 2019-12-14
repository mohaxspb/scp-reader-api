package ru.kuchanov.scpreaderapi.repository.article.image

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.image.ArticlesImages
import ru.kuchanov.scpreaderapi.model.dto.article.ArticlesImagesDto

interface ArticlesImagesRepository : JpaRepository<ArticlesImages, Long> {

    fun findAllByArticleForLangId(articleForLangId: Long): List<ArticlesImagesDto>

    @Query("select a from ArticlesImages a where a.articleForLangId = :articleForLangId")
    fun findAllByArticleForLangIdFull(articleForLangId: Long): List<ArticlesImages>
}