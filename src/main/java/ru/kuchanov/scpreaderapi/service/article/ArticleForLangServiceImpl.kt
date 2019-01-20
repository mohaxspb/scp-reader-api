package ru.kuchanov.scpreaderapi.service.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.repository.article.ArticlesForLangRepository
import ru.kuchanov.scpreaderapi.repository.article.ArticlesImagesRepository
import ru.kuchanov.scpreaderapi.repository.article.tags.TagForLangRepository


@Suppress("unused")
@Service
class ArticleForLangServiceImpl : ArticleForLangService {

    @Autowired
    private lateinit var articlesForLangRepository: ArticlesForLangRepository

    @Autowired
    private lateinit var imagesRepository: ArticlesImagesRepository

    @Autowired
    private lateinit var tagsForLangRepository: TagForLangRepository

    override fun findAll() =
            articlesForLangRepository.findAll().toList()

    override fun insert(articleForLang: ArticleForLang): ArticleForLang =
            articlesForLangRepository.save(articleForLang)

    override fun insert(articleForLang: List<ArticleForLang>): List<ArticleForLang> =
            articlesForLangRepository.saveAll(articleForLang)

    override fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String) =
            articlesForLangRepository.getArticleForLangByUrlRelativeAndLangId(urlRelative, langId)

    override fun getIdByUrlRelativeAndLangId(urlRelative: String, langId: String) =
            articlesForLangRepository.getIdByUrlRelativeAndLangId(urlRelative, langId)

    //FUCKING SHIT!!!!!!!!!!!!!!!!!!!!
    //todo try overrided pageable with offset/limit support
    override fun getMostRecentArticlesForLang(langId: String, offset: Int, limit: Int) =
            articlesForLangRepository.getMostRecentArticlesForLang(langId, offset, limit).map {
                //shit is here, blyat
                it.apply {
                    imageUrls = imagesRepository.findAllByArticleForLangId(articleForLangId = it.id)
                    tagsForLang = tagsForLangRepository.getAllForLangIdAndArticleForLangIdAsDto(
                            langId = langId,
                            articleForLangId = it.id
                    )
                }
            }

    override fun getMostRecentArticlesForLangFull(langId: String, offset: Int, limit: Int) =
            articlesForLangRepository.getMostRecentArticlesForLangFull(langId, offset, limit).map {
                it.apply {
                    images = imagesRepository.findAllByArticleForLangIdFull(articleForLangId = id!!).toMutableSet()
                    tags = tagsForLangRepository.getAllForLangIdAndArticleForLangId(
                            langId = langId,
                            articleForLangId = id
                    ).toMutableSet()
                }
            }

    override fun getOneByLangAndArticleId(articleId: Long, langId: String) =
            articlesForLangRepository.getOneByArticleIdAndLangId(articleId, langId)
}