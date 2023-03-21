package ru.kuchanov.scpreaderapi.controller.article

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Cache.Keys.ARTICLE_TO_LANG_DTO_BY_URL_RELATIVE_AND_LANG
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Cache.Keys.CATEGORIES_ARTICLES
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Cache.Keys.SEARCH_RESULTS_CACHE
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLangNotFoundException
import ru.kuchanov.scpreaderapi.bean.articles.ArticleNotFoundException
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForLangNotFoundException
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.isAdmin
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleToLangDto
import ru.kuchanov.scpreaderapi.model.exception.ScpAccessDeniedException
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.category.ArticleCategoryForLangService
import ru.kuchanov.scpreaderapi.service.article.category.ArticleCategoryService
import ru.kuchanov.scpreaderapi.service.article.text.TextPartService
import ru.kuchanov.scpreaderapi.service.search.SearchStatsService
import ru.kuchanov.scpreaderapi.service.users.LangService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.ARTICLE)
class ArticleController @Autowired constructor(
    private val articleForLangService: ArticleForLangService,
    private val langService: LangService,
    private val categoryForLangService: ArticleCategoryForLangService,
    private val categoryService: ArticleCategoryService,
    private val textPartService: TextPartService,
    private val searchStatsService: SearchStatsService,
    private val cacheManager: CacheManager,
    private val log: Logger,
) {

    @GetMapping("/{langEnum}/recent")
    fun showRecentArticles(
        @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
        @RequestParam(value = "offset") offset: Int,
        @RequestParam(value = "limit") limit: Int
    ): List<ArticleToLangDto> =
        articleForLangService.getMostRecentArticlesForLang(langEnum.lang, offset, limit)

    @GetMapping("/{langEnum}/rated")
    fun showRatedArticles(
        @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
        @RequestParam(value = "offset") offset: Int,
        @RequestParam(value = "limit") limit: Int
    ): List<ArticleToLangDto> =
        articleForLangService.getMostRatedArticlesForLang(langEnum.lang, offset, limit)

    @GetMapping("/{langEnum}/category/{categoryId}")
    fun getArticlesByCategoryAndLang(
        @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
        @PathVariable(value = "categoryId") categoryId: Long
    ): List<ArticleToLangDto> {
//        log.error("getArticlesByCategoryAndLang: $langEnum, $categoryId")
        val articlesCache = cacheManager.getCache(CATEGORIES_ARTICLES)
        val rawArticlesInCache = articlesCache?.get(SimpleKey(langEnum, categoryId))
        @Suppress("UNCHECKED_CAST") val categoriesArticlesInCache =
            rawArticlesInCache?.get() as? List<ArticleToLangDto>?

        return if (categoriesArticlesInCache != null) {
            categoriesArticlesInCache
        } else {
            val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
            val category = categoryService.getById(categoryId)
                ?: throw ArticleCategoryNotFoundException()
            val articleCategoryToLang = categoryForLangService.findByLangIdAndArticleCategoryId(
                langId = lang.id,
                articleCategoryId = category.id!!
            )
                ?: throw ArticleCategoryForLangNotFoundException()
            val articlesToCategoryForCache = articleForLangService
                .findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLang.id!!)
            articlesCache
                ?.put(
                    SimpleKey(langEnum, categoryId),
                    articlesToCategoryForCache
                )
            articlesToCategoryForCache
        }
    }

    @GetMapping("{langEnum}/{id}")
    fun showArticleForLangAndId(
        @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
        @PathVariable(value = "id") articleId: Long
    ): ArticleForLang =
        articleForLangService.getOneByLangIdAndArticleId(articleId, langEnum.lang)
            ?: throw ArticleForLangNotFoundException()

    @GetMapping("{id}")
    fun showArticleForLangById(
        @PathVariable(value = "id") articleToLangId: Long
    ): ArticleToLangDto {
//        log.error("showArticleForLangById: $articleToLangId")
        val articlesByIdCache = cacheManager.getCache(ScpReaderConstants.Cache.Keys.ARTICLE_TO_LANG_DTO_BY_ID)
        val rawArticleInCache = articlesByIdCache?.get(SimpleKey(articleToLangId))
        val articleInCache = rawArticleInCache?.get() as? ArticleToLangDto?
        return articleInCache
            ?: articleForLangService.getOneByIdAsDto(articleToLangId)
            ?: throw ArticleNotFoundException()
    }

    @GetMapping("{langEnum}/{id}/full")
    fun showArticleForIdAndLangIdFull(
        @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
        @PathVariable(value = "id") articleId: Long
    ): ArticleToLangDto {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        return articleForLangService.getOneByLangIdAndArticleIdAsDto(articleId, lang.id)
            ?: throw ArticleForLangNotFoundException()
    }

    @GetMapping("{langEnum}/full")
    fun showArticleForUrlRelativeAndLangIdFull(
        @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
        @RequestParam(value = "urlRelative") urlRelative: String
    ): ArticleToLangDto {
//        log.error("showArticleForUrlRelativeAndLangIdFull: $langEnum$urlRelative")
        val articlesCache = cacheManager.getCache(ARTICLE_TO_LANG_DTO_BY_URL_RELATIVE_AND_LANG)
        val rawArticleInCache = articlesCache?.get(SimpleKey(langEnum, urlRelative))
        val articleInCache = rawArticleInCache?.get() as? ArticleToLangDto?
        return articleInCache
            ?: articleForLangService.getArticleForLangByUrlRelativeAndLangAsDto(
                urlRelative,
                langService.getById(langEnum.lang)?.id ?: throw LangNotFoundException()
            )
            ?: throw ArticleNotFoundException()
    }

    @DeleteMapping("{langEnum}/{articleId}/delete")
    fun deleteArticleTextPartsByLangAndArticleId(
        @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
        @PathVariable(value = "articleId") articleId: Long,
        @AuthenticationPrincipal user: User
    ): Boolean {
        if (user.isAdmin().not()) {
            throw ScpAccessDeniedException()
        }
        log.error("deleteArticleTextPartsByLangAndArticleId: $langEnum, $articleId")
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        val articleToLangId = articleForLangService.getOneByLangIdAndArticleId(articleId, lang.id)?.id
            ?: throw ArticleForLangNotFoundException()
        textPartService.deleteByArticleToLangId(articleToLangId)
        return true
    }

    @GetMapping("{langEnum}/random")
    fun getRandomArticle(
        @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance
    ): ArticleToLangDto {
        val lang = langEnum.lang.let { langService.getById(it) ?: throw LangNotFoundException() }
        return articleForLangService.getRandomArticle(lang.id)
    }

    @GetMapping("{langEnum}/search")
    fun search(
        @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
        @RequestParam(value = "query") query: String,
        @RequestParam(value = "offset") offset: Int,
        @RequestParam(value = "limit") limit: Int
    ): List<ArticleToLangDto> {
        val articlesCache = cacheManager.getCache(SEARCH_RESULTS_CACHE)
        val key = SimpleKey(query, limit, offset)

        searchStatsService.upsertSearchStats(langEnum.lang, query)
        //todo count num of requests for query (create table to store requests (lang + query + counter))
        //later we can populate cache with search results for most popular queries
        //I.e. we can get top 100 requests and add to cache first 100 rows for query...
        //do iterate by results to put paginated results i.e. put(EN, reptile, 0, 10), put(EN, reptile, 10, 20) etc

        val rawArticlesInCache = articlesCache?.get(key)
        @Suppress("UNCHECKED_CAST") val categoriesArticlesInCache =
            rawArticlesInCache?.get() as? List<ArticleToLangDto>?
        return if (categoriesArticlesInCache != null) {
            categoriesArticlesInCache
        } else {
            val lang = langEnum.lang.let { langService.getById(it) ?: throw LangNotFoundException() }
            log.debug("query: $query")
            val searchResults = articleForLangService.search(lang.id, query, offset, limit)
            articlesCache?.put(key, searchResults)
            searchResults
        }
    }
}
