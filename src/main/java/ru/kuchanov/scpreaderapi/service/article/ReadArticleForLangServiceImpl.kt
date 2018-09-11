package ru.kuchanov.scpreaderapi.service.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ReadArticlesByLang
import ru.kuchanov.scpreaderapi.repository.article.ReadArticlesForLangRepository


@Service
class ReadArticleForLangServiceImpl : ReadArticleForLangService {

    @Autowired
    private lateinit var repository: ReadArticlesForLangRepository

    override fun findAll() = repository.findAll().toList()

    override fun update(articleForLang: ReadArticlesByLang): ReadArticlesByLang = repository.save(articleForLang)

    override fun insert(article: ReadArticlesByLang): ReadArticlesByLang = repository.save(article)

    override fun insert(articles: List<ReadArticlesByLang>): List<ReadArticlesByLang> = repository.saveAll(articles)

    override fun getReadArticleForArticleIdLangIdAndUserId(articleId: Long, langId: String, userId: Long) =
            repository.getReadArticleForArticleIdLangIdAndUserId(articleId, langId, userId)
}