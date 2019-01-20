package ru.kuchanov.scpreaderapi.service.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLangToArticleForLang
import ru.kuchanov.scpreaderapi.repository.article.ArticleForLangToArticleForLangRepository

@Service
class ArticleForLangToArticleForLangServiceImpl : ArticleForLangToArticleForLangService {

    @Autowired
    private lateinit var repository: ArticleForLangToArticleForLangRepository

    override fun findAll(): List<ArticleForLangToArticleForLang> =
            repository.findAll()

    override fun insert(data: ArticleForLangToArticleForLang): ArticleForLangToArticleForLang =
            repository.save(data)

    override fun insert(data: List<ArticleForLangToArticleForLang>): List<ArticleForLangToArticleForLang> =
            repository.saveAll(data)

    override fun getOneById(id: Long): ArticleForLangToArticleForLang? =
            repository.getOneById(id)

    override fun findByParentArticleForLangIdAndArticleForLangId(
            parentArticleForLangId: Long,
            articleForLangId: Long
    ): ArticleForLangToArticleForLang? =
            repository.findByParentArticleForLangIdAndArticleForLangId(parentArticleForLangId, articleForLangId)

    override fun getAllByArticleForLangId(articleForLangId: Long): List<ArticleForLangToArticleForLang> =
            getAllByArticleForLangId(articleForLangId)

    override fun getAllByParentArticleForLangId(articleForLangId: Long): List<ArticleForLangToArticleForLang> =
            repository.getAllByParentArticleForLangId(articleForLangId)
}