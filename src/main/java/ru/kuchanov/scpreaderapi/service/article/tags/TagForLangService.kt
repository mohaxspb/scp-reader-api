package ru.kuchanov.scpreaderapi.service.article.tags

import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForLang
import javax.transaction.Transactional

interface TagForLangService {

    fun findOneById(id: Long): TagForLang?

    fun findAll(): List<TagForLang>

    @Transactional
    fun insert(data: TagForLang): TagForLang

    fun insert(data: List<TagForLang>): List<TagForLang>
}