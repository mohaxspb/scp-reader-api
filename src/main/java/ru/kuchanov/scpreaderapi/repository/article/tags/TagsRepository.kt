package ru.kuchanov.scpreaderapi.repository.article.tags

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.tags.Tag

interface TagsRepository : JpaRepository<Tag, Long> {

    fun getOneById(id: Long): Tag?
}