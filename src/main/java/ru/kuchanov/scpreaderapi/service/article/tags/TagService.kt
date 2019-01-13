package ru.kuchanov.scpreaderapi.service.article.tags

import ru.kuchanov.scpreaderapi.bean.articles.tags.Tag
import javax.transaction.Transactional

interface TagService {

    fun findOneById(id: Long): Tag?

    fun findAll(): List<Tag>

    @Transactional
    fun insert(tag: Tag): Tag

    fun insert(tags: List<Tag>): List<Tag>
}