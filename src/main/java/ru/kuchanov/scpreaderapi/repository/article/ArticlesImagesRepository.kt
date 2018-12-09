package ru.kuchanov.scpreaderapi.repository.article

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.ArticlesImages

interface ArticlesImagesRepository : JpaRepository<ArticlesImages, Long> {

    //todo
}