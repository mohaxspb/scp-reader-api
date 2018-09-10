package ru.kuchanov.scpreaderapi.service.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.Article
import ru.kuchanov.scpreaderapi.bean.articles.ArticleNotFoundException
import ru.kuchanov.scpreaderapi.repository.article.ArticlesRepository


@Service
class ArticleServiceImpl : ArticleService {

    @Autowired
    private lateinit var repository: ArticlesRepository

    override fun findAll() = repository.findAll().toList()

    override fun getById(id: Long) = repository.getOne(id) ?: throw ArticleNotFoundException()

    override fun update(article: Article): Article = repository.save(article)

    override fun insert(article: Article): Article = repository.save(article)

    override fun insert(articles: List<Article>): List<Article> = repository.saveAll(articles)

    override fun getArticleByUrlRelativeAndLang(urlRelative: String, langId: String) =
            repository.getOneByUrlRelativeUrlAndLang(urlRelative, langId)

//    override fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String) =
//            repository.getArticleForLangByUrlRelativeAndLang(urlRelative, langId)
}