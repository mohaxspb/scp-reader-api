package ru.kuchanov.scpreaderapi.repository.article.tags

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForLang

interface TagForLangRepository : JpaRepository<TagForLang, Long> {

    fun getOneById(id: Long): TagForLang?

    fun findOneByLangIdAndTitle(langId: String, title: String): TagForLang?

    fun getAllByTagId(tagId: Long): List<TagForLang>

    fun getAllByLangId(langId: String): List<TagForLang>

    @Query(
            value = """
                SELECT * FROM tags_langs tl
                JOIN tags_articles_langs tal
                ON tal.tag_for_lang_id = tl.id AND tal.article_for_lang_id = :articleForLangId
                WHERE tl.lang_id = :langId
            """,
            nativeQuery = true
    )
    fun getAllForLangIdAndArticleForLangId(langId: String, articleForLangId: Long): List<TagForLang>
}