package ru.kuchanov.scpreaderapi.service.article.favorite

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.favorite.FavoriteArticlesByLang
import ru.kuchanov.scpreaderapi.repository.article.favorite.FavoriteArticlesForLangRepository


@Service
class FavoriteArticleForLangServiceImpl : FavoriteArticleForLangService {

    @Autowired
    private lateinit var repository: FavoriteArticlesForLangRepository

    override fun findAll() = repository.findAll().toList()

    override fun update(articleForLang: FavoriteArticlesByLang): FavoriteArticlesByLang = repository.save(articleForLang)

    override fun insert(article: FavoriteArticlesByLang): FavoriteArticlesByLang = repository.save(article)

    override fun insert(articles: List<FavoriteArticlesByLang>): List<FavoriteArticlesByLang> = repository.saveAll(articles)

    override fun getFavoriteArticleForArticleIdLangIdAndUserId(articleId: Long, langId: String, userId: Long) =
            repository.getFavoriteArticleForArticleIdLangIdAndUserId(articleId, langId, userId)
}