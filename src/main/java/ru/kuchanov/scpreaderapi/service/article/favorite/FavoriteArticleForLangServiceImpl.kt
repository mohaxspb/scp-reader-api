package ru.kuchanov.scpreaderapi.service.article.favorite

import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.favorite.FavoriteArticlesByLang
import ru.kuchanov.scpreaderapi.repository.article.favorite.FavoriteArticlesForLangRepository


@Service
class FavoriteArticleForLangServiceImpl constructor(
        val repository: FavoriteArticlesForLangRepository
) : FavoriteArticleForLangService {

    override fun deleteById(id: Long) =
            repository.deleteById(id)

    override fun save(article: FavoriteArticlesByLang): FavoriteArticlesByLang =
            repository.save(article)

    override fun getFavoriteArticleForArticleIdLangIdAndUserId(articleToLangId: Long, userId: Long) =
            repository.findByArticleToLangIdAndUserId(articleToLangId, userId)
}
