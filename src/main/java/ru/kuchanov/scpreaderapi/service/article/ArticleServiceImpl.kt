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

    override fun getById(id: Long): Article =
            repository.getOne(id) ?: throw ArticleNotFoundException()

    override fun save(article: Article): Article =
            repository.save(article)

    override fun getArticleByUrlRelative(urlRelative: String): Article? =
            repository.getArticleByUrlRelative(urlRelative)

    override fun getArticlesByUrlRelative(urlRelative: String): List<Article> =
            repository.getArticlesByUrlRelative(urlRelative)

    override fun getArticleByUrlRelativeAndLang(urlRelative: String, langId: String): Article? =
            repository.getOneByUrlRelativeUrlAndLang(urlRelative, langId)

    override fun getCreatedArticlesBetweenDates(startDate: String, endDate: String): List<Article> =
            repository.getCreatedArticlesBetweenDates(startDate, endDate)
}
