package ru.kuchanov.scpreaderapi.repository.article

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.Article
import ru.kuchanov.scpreaderapi.bean.users.Lang

interface ArticlesRepository : JpaRepository<Article, Long> {
    fun findOneById(id: Long): Lang

    @Query("SELECT a FROM Article a " +
            "JOIN ArticlesLangs al ON a.id = al.articleId " +
            "WHERE al.urlRelative = :urlRelative AND al.langId = :langId")
    fun getOneByUrlRelativeUrlAndLang(urlRelative: String, langId: String): Article?
}