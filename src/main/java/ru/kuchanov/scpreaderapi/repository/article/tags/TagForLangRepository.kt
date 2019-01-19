package ru.kuchanov.scpreaderapi.repository.article.tags

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForLang
import ru.kuchanov.scpreaderapi.model.dto.article.TagForLangDto

interface TagForLangRepository : JpaRepository<TagForLang, Long> {

    fun getOneById(id: Long): TagForLang?

    fun findOneByLangIdAndTitle(langId: String, title: String): TagForLang?

    fun getAllByTagId(tagId: Long): List<TagForLang>

    fun getAllByLangId(langId: String): List<TagForLang>

    @Query(
            """
                SELECT tl
                FROM TagForLang tl
                JOIN TagForArticleForLang tal
                ON tal.tagForLangId = tl.id AND tal.articleForLangId = :articleForLangId
                WHERE tl.langId = :langId
            """
    )
    fun getAllForLangIdAndArticleForLangId(langId: String, articleForLangId: Long): List<TagForLang>

    @Query(
            value =
            """
                SELECT tl.id, tl.title FROM tags_langs tl
                JOIN tags_articles_langs tal
                ON tal.tag_for_lang_id = tl.id AND tal.article_for_lang_id = :articleForLangId
                WHERE tl.lang_id = :langId
            """,
            nativeQuery = true
    )
    fun getAllForLangIdAndArticleForLangIdAsDto(langId: String, articleForLangId: Long): List<TagForLangDto>
}
