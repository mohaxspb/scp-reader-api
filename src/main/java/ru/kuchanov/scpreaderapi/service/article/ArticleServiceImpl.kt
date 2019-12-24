package ru.kuchanov.scpreaderapi.service.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.Article
import ru.kuchanov.scpreaderapi.bean.articles.ArticleNotFoundException
import ru.kuchanov.scpreaderapi.repository.article.ArticlesRepository


@Service
class ArticleServiceImpl @Autowired constructor(
        val repository: ArticlesRepository
) : ArticleService {

    override fun getById(id: Long) =
            repository.getOne(id) ?: throw ArticleNotFoundException()

    override fun insert(article: Article): Article =
            repository.save(article)

    override fun getArticleByUrlRelative(urlRelative: String) =
            repository.getArticleByUrlRelative(urlRelative)

    override fun getArticleByUrlRelativeAndLang(urlRelative: String, langId: String) =
            repository.getOneByUrlRelativeUrlAndLang(urlRelative, langId)
}
