package ru.kuchanov.scpreaderapi.service.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.repository.article.ArticlesForLangRepository
import ru.kuchanov.scpreaderapi.repository.article.ArticlesImagesRepository


@Suppress("unused")
@Service
class ArticleForLangServiceImpl : ArticleForLangService {

    @Autowired
    private lateinit var repository: ArticlesForLangRepository

    @Autowired
    private lateinit var imagesRepository: ArticlesImagesRepository

    override fun findAll() =
            repository.findAll().toList()

    override fun update(articleForLang: ArticleForLang): ArticleForLang =
            repository.save(articleForLang)

    override fun insert(articleForLang: ArticleForLang): ArticleForLang =
            repository.save(articleForLang)

    override fun insert(articleForLang: List<ArticleForLang>): List<ArticleForLang> =
            repository.saveAll(articleForLang)

    override fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String) =
            repository.getArticleForLangByUrlRelativeAndLang(urlRelative, langId)

    override fun getArticleForLang(articleId: Long, langId: String) =
            repository.getArticleForLang(articleId, langId)

    //FUCKING SHIT!!!!!!!!!!!!!!!!!!!!
    //todo try overrided pageable with offset/limit support
    override fun getMostRecentArticlesForLang(langId: String, offset: Int, limit: Int) =
            repository.getMostRecentArticlesForLang(langId, offset, limit).map {
                //shit is here, blyat
                it.apply {
                    imageUrls = imagesRepository.findAllByArticleUrlRelativeAndArticleLangIdAndArticleId(
                            articleUrlRelative = it.urlRelative,
                            articleLangId = it.langId,
                            articleId = it.articleId
                    )
                }
            }

    override fun getOneByLangAndArticleId(articleId: Long, langId: String) =
            repository.getOneByArticleIdAndLangId(articleId, langId)
}