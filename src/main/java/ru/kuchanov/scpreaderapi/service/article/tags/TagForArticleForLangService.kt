package ru.kuchanov.scpreaderapi.service.article.tags

import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForArticleForLang
import javax.transaction.Transactional

interface TagForArticleForLangService {

    fun findOneById(id: Long): TagForArticleForLang?

    fun findAll(): List<TagForArticleForLang>

    @Transactional
    fun insert(data: TagForArticleForLang): TagForArticleForLang

    fun insert(data: List<TagForArticleForLang>): List<TagForArticleForLang>
}