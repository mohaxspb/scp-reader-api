package ru.kuchanov.scpreaderapi.repository.article

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLangToArticleForLang

interface ArticleForLangToArticleForLangRepository : JpaRepository<ArticleForLangToArticleForLang, Long> {

    fun getOneById(id: Long): ArticleForLangToArticleForLang?

    fun findByArticleForLangIdAndParentArticleForLangId(
            parentArticleForLangId: Long,
            articleForLangId: Long
    ): ArticleForLangToArticleForLang?

    /**
     * returns all parents for given articleForLangId
     */
    fun getAllByArticleForLangId(articleForLangId: Long): List<ArticleForLangToArticleForLang>

    /**
     * returns all inner articles for given articleForLangId
     */
    fun getAllByParentArticleForLangId(articleForLangId: Long): List<ArticleForLangToArticleForLang>
}