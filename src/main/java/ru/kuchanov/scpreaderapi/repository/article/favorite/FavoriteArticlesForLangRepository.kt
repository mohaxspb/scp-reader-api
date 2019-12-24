package ru.kuchanov.scpreaderapi.repository.article.favorite

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.favorite.FavoriteArticlesByLang

interface FavoriteArticlesForLangRepository : JpaRepository<FavoriteArticlesByLang, Long> {

    fun findByArticleToLangIdAndUserId(articleToLangId: Long, userId: Long): FavoriteArticlesByLang?
}
