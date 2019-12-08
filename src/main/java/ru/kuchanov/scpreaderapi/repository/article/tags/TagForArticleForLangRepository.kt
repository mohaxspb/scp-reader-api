package ru.kuchanov.scpreaderapi.repository.article.tags

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForArticleForLang

interface TagForArticleForLangRepository : JpaRepository<TagForArticleForLang, Long> {

    fun getOneById(id: Long): TagForArticleForLang?

    fun getAllByArticleForLangId(articleForLangId: Long): List<TagForArticleForLang>

    fun getOneByTagForLangIdAndArticleForLangId(tagForLangId: Long, articleForLangId: Long): TagForArticleForLang?
}