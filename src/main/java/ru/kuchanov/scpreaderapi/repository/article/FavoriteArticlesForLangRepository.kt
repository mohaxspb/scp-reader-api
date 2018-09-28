package ru.kuchanov.scpreaderapi.repository.article

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.FavoriteArticlesByLang
import ru.kuchanov.scpreaderapi.bean.articles.KeyFavoriteArticleByLang

interface FavoriteArticlesForLangRepository : JpaRepository<FavoriteArticlesByLang, KeyFavoriteArticleByLang> {
    @Query("SELECT fa FROM FavoriteArticlesByLang fa " +
            "WHERE fa.articleId = :articleId AND fa.langId = :langId AND fa.userId = :userId")
    fun getFavoriteArticleForArticleIdLangIdAndUserId(articleId: Long, langId: String, userId: Long): FavoriteArticlesByLang?
}