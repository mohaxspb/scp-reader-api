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
            name = TagForLang.NATIVE_QUERY_ALL_FOR_LANG_ID_AND_ARTICLE_FOR_LANG_ID,
            nativeQuery = true
    )
    fun getAllForLangIdAndArticleForLangId(langId: String, articleForLangId: Long): List<TagForLang>

    @Query(
            name = TagForLang.NATIVE_QUERY_ALL_FOR_LANG_ID_AND_ARTICLE_FOR_LANG_ID,
            nativeQuery = true
    )
    fun getAllForLangIdAndArticleForLangIdAsDto(langId: String, articleForLangId: Long): List<TagForLangDto>
}
