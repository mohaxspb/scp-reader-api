package ru.kuchanov.scpreaderapi.repository.article.tags

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForLang

interface TagForLangRepository : JpaRepository<TagForLang, Long> {

    fun getOneById(id: Long): TagForLang?

    fun getAllByTagId(tagId: Long): List<TagForLang>
    fun getAllByLangId(langId: String): List<TagForLang>
}