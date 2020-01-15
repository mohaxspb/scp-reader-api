package ru.kuchanov.scpreaderapi.service.article.tags

import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForLang
import javax.transaction.Transactional

interface TagForLangService {

    fun findOneById(id: Long): TagForLang?

    fun findOneByLangIdAndTitle(langId: String, title: String): TagForLang?

    fun findAll(): List<TagForLang>

    @Transactional
    fun insert(data: TagForLang): TagForLang
}
