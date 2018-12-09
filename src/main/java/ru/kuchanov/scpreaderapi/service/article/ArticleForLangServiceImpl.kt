package ru.kuchanov.scpreaderapi.service.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.repository.article.ArticlesForLangRepository


@Suppress("unused")
@Service
class ArticleForLangServiceImpl : ArticleForLangService {

    @Autowired
    private lateinit var repository: ArticlesForLangRepository

    override fun findAll() = repository.findAll().toList()

    override fun update(articleForLang: ArticleForLang): ArticleForLang = repository.save(articleForLang)

    override fun insert(articleForLang: ArticleForLang): ArticleForLang = repository.save(articleForLang)

    override fun insert(articleForLang: List<ArticleForLang>): List<ArticleForLang> = repository.saveAll(articleForLang)

    override fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String) =
            repository.getArticleForLangByUrlRelativeAndLang(urlRelative, langId)

    override fun getArticleForLang(articleId: Long, langId: String) = repository.getArticleForLang(articleId, langId)

    override fun getMostRecentArticlesForLang(langId: String, offset: Int, limit: Int) =
            repository.getMostRecentArticlesForLang(langId, offset, limit)

    override fun getOneByLangAndArticleId(articleId: Long, langId: String) =
            repository.getOneByArticleIdAndLangId(articleId, langId)
}