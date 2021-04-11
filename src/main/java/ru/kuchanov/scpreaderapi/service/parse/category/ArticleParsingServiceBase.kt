package ru.kuchanov.scpreaderapi.service.parse.category

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.http.util.TextUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import org.jsoup.select.Elements
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.context.annotation.Primary
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.Application
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Cache.Keys.ARTICLE_TO_LANG_DTO_BY_ID
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Cache.Keys.ARTICLE_TO_LANG_DTO_BY_URL_RELATIVE_AND_LANG
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Cache.Keys.CATEGORIES_ARTICLES
import ru.kuchanov.scpreaderapi.bean.articles.Article
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLangToArticleForLang
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForLangToArticleForLang
import ru.kuchanov.scpreaderapi.bean.articles.error.ArticleParseError
import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForLang
import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart
import ru.kuchanov.scpreaderapi.bean.articles.types.ArticlesAndArticleTypes
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.configuration.NetworkConfiguration
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangToArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.ArticleService
import ru.kuchanov.scpreaderapi.service.article.category.ArticleCategoryForArticleService
import ru.kuchanov.scpreaderapi.service.article.category.ArticleCategoryForLangService
import ru.kuchanov.scpreaderapi.service.article.error.ArticleParseErrorService
import ru.kuchanov.scpreaderapi.service.article.image.ArticlesImagesService
import ru.kuchanov.scpreaderapi.service.article.tags.TagForArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.tags.TagForLangService
import ru.kuchanov.scpreaderapi.service.article.tags.TagService
import ru.kuchanov.scpreaderapi.service.article.text.TextPartService
import ru.kuchanov.scpreaderapi.service.article.type.ArticleAndArticleTypeService
import ru.kuchanov.scpreaderapi.service.article.type.ArticleTypeService
import ru.kuchanov.scpreaderapi.service.mail.MailService
import ru.kuchanov.scpreaderapi.service.parse.article.ParseArticleService
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ATTR_HREF
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ATTR_SRC
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.CLASS_SPOILER
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ID_PAGE_CONTENT
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_A
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_BODY
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_IMG
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_P
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_TABLE
import ru.kuchanov.scpreaderapi.service.users.LangService
import ru.kuchanov.scpreaderapi.utils.ErrorUtils
import java.io.IOException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.transaction.Transactional
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import ru.kuchanov.scpreaderapi.bean.articles.tags.Tag as ArticleTag


@Primary
@Service
class ArticleParsingServiceBase {

    @Autowired
    protected lateinit var autowireCapableBeanFactory: AutowireCapableBeanFactory

    @Autowired
    private lateinit var mailService: MailService

    @Qualifier(Application.PARSING_LOGGER)
    @Autowired
    private lateinit var log: Logger

    @Autowired
    @Qualifier(NetworkConfiguration.QUALIFIER_OK_HTTP_CLIENT_NOT_LOGGING)
    protected lateinit var okHttpClient: OkHttpClient

    @Autowired
    private lateinit var langService: LangService

    @Autowired
    private lateinit var parseArticleService: ParseArticleService

    @Autowired
    private lateinit var articleService: ArticleService

    @Autowired
    private lateinit var articleForLangService: ArticleForLangService

    @Autowired
    private lateinit var articlesImagesService: ArticlesImagesService

    @Autowired
    private lateinit var articleForLangToArticleForLangService: ArticleForLangToArticleForLangService

    @Autowired
    private lateinit var tagService: TagService

    @Autowired
    private lateinit var tagForLangService: TagForLangService

    @Autowired
    private lateinit var tagForArticleForLangService: TagForArticleForLangService

    @Autowired
    private lateinit var articleTypeService: ArticleTypeService

    @Autowired
    private lateinit var articleAndArticleTypeService: ArticleAndArticleTypeService

    @Autowired
    private lateinit var categoryToArticleService: ArticleCategoryForArticleService

    @Autowired
    private lateinit var categoryToLangService: ArticleCategoryForLangService

    @Autowired
    private lateinit var textPartService: TextPartService

    @Autowired
    lateinit var articleParseErrorService: ArticleParseErrorService

    @Autowired
    lateinit var errorUtils: ErrorUtils

    @Autowired
    lateinit var cacheManager: CacheManager

    var isDownloadAllRunning = false

    fun getParsingRealizationForLang(lang: Lang): ArticleParsingServiceBase =
            when (lang.langCode) {
                ScpReaderConstants.Firebase.FirebaseInstance.RU.lang -> getBean(ArticleParsingServiceImplRU::class)
                ScpReaderConstants.Firebase.FirebaseInstance.EN.lang -> getBean(ArticleParsingServiceImplEN::class)
                ScpReaderConstants.Firebase.FirebaseInstance.DE.lang -> getBean(ArticleParsingServiceImplDE::class)
                ScpReaderConstants.Firebase.FirebaseInstance.FR.lang -> getBean(ArticleParsingServiceImplFR::class)
                ScpReaderConstants.Firebase.FirebaseInstance.ES.lang -> getBean(ArticleParsingServiceImplES::class)
                ScpReaderConstants.Firebase.FirebaseInstance.IT.lang -> getBean(ArticleParsingServiceImplIT::class)
                ScpReaderConstants.Firebase.FirebaseInstance.PL.lang -> getBean(ArticleParsingServiceImplPL::class)
                ScpReaderConstants.Firebase.FirebaseInstance.PT.lang -> getBean(ArticleParsingServiceImplPT::class)
                ScpReaderConstants.Firebase.FirebaseInstance.ZH.lang -> getBean(ArticleParsingServiceImplCH::class)
                else -> throw NotImplementedError("No parsing realization, need lang(current lang: $lang)")
            }

    private fun <BEAN : ArticleParsingServiceBase> getBean(clazz: KClass<out BEAN>): BEAN {
        val bean = clazz.createInstance()
        autowireCapableBeanFactory.autowireBean(bean)
        autowireCapableBeanFactory.initializeBean(bean, clazz.simpleName!!)
        return bean
    }

    fun getRatedArticlesUrl() = "/top-rated-pages/p/"

    fun getRecentArticlesUrl() = "/most-recently-created/p/"

    fun getObjectArticlesUrls() = listOf(
            "/scp-list",
            "/scp-list-2",
            "/scp-list-3",
            "/scp-list-4",
            "/scp-list-5",
            "/scp-list-6"
    )

    fun getArticleRatingStringDelimiter() = ", рейтинг"

    fun getArticleRatingStringDelimiterEnd() = ""

    @Async
    fun parseEverything(
            maxPageCount: Int? = null,
            processOnlyCount: Int? = null,
            downloadRecent: Boolean = true,
            downloadObjects: Boolean = true,
            sendMail: Boolean
    ) {
        isDownloadAllRunning = true

        val startTime = System.currentTimeMillis()

        ScpReaderConstants.Firebase.FirebaseInstance.values().toFlowable()
                .parallel()
                .runOn(Schedulers.newThread())
                .map { langService.getById(it.lang) ?: throw LangNotFoundException() }
                .map { it to getParsingRealizationForLang(it) }
                .concatMap { (lang, service) ->
                    Flowable
                            .fromIterable(service.getObjectArticlesUrls())
                            .concatMapCompletable { objectsUrl ->
                                if (downloadObjects) {
                                    service
                                            .downloadAndSaveObjectArticles(
                                                    lang,
                                                    objectsUrl,
                                                    processOnlyCount = processOnlyCount
                                            )
                                            .doOnSubscribe {
                                                log.info("Start loading objects ($objectsUrl) for lang ${lang.id}")
                                            }
                                            .doOnSuccess {
                                                log.info("Done loading objects ($objectsUrl) for lang ${lang.id}. Saved: ${it.size}")
                                            }
                                            .ignoreElement()
                                } else {
                                    Completable.complete()
                                }
                            }
                            .andThen(
                                    if (downloadRecent) {
                                        service
                                                .downloadAndSaveAllRecentArticles(
                                                        lang,
                                                        maxPageCount = maxPageCount,
                                                        processOnlyCount = processOnlyCount
                                                )
                                                .doOnSubscribe {
                                                    log.info("Start downloadAndSaveAllRecentArticles for lang: ${lang.id}")
                                                }
                                                .doOnSuccess {
                                                    log.info("Finish downloadAndSaveAllRecentArticles for lang ${lang.id}: ${it.size}")
                                                }
                                                .ignoreElement()
                                    } else {
                                        Completable.complete()
                                    }
                            )
                            //maybe run it separately
//                            .andThen(
//                                    service
//                                            .downloadAndSaveAllRatedArticles(lang)
//                                            .doOnSuccess { log.error("Rated saved: ${it.size}") }
//                                            .ignoreElement()
//                            )
                            .toFlowable<Nothing>()
                            .doOnSubscribe { log.info("Articles save started for lang: ${lang.id}") }
                            .doOnComplete { log.info("Articles save ended for lang: ${lang.id}") }
                }
                .sequential()
                .ignoreElements()
                .doOnEvent { doOnDownloadEverythingComplete(startTime, it, sendMail) }
                .subscribeBy(
                        onComplete = { log.info("Download everything completed!") },
                        onError = {
                            log.error("Error while parse everything")
                            log.error("Error while parseEverything", it)
                        }
                )
    }

    private fun doOnDownloadEverythingComplete(
            startTime: Long,
            error: Throwable? = null,
            sendMail: Boolean
    ) {
        isDownloadAllRunning = false

        val timeSpent = System.currentTimeMillis() - startTime
        val minutesSpent = TimeUnit.MILLISECONDS.toMinutes(timeSpent)

        val errorNotOccurred = error == null
        val subj = if (errorNotOccurred) {
            "Sync all sites data finished successfully"
        } else {
            "Sync all sites data finished with error: $error"
        }
        val message = if (errorNotOccurred) {
            "Sync all sites data done in $minutesSpent minutes."
        } else {
            "Sync all sites data failed after $minutesSpent minutes.\n\n Error:\n${errorUtils.stackTraceAsString(error)}"
        }
        log.error(message)
        if (sendMail) {
            mailService.sendMail(mailService.getAdminAddress(), subj = subj, text = message)
        }
    }

    @Async
    fun parseObjectsArticlesForLang(
            lang: Lang,
            totalPageCount: Int? = null,
            processOnlyCount: Int? = null,
            innerArticlesDepth: Int? = null
    ) {
        Flowable.fromIterable(getObjectArticlesUrls())
                .flatMapSingle { objectsUrl ->
                    downloadAndSaveObjectArticles(
                            lang = lang,
                            objectsUrl = objectsUrl,
                            processOnlyCount = processOnlyCount,
                            innerArticlesDepth = innerArticlesDepth
                    )
                }
                .toList()
                .map { it.flatten() }
                .subscribeBy(
                        onSuccess = { articlesDownloadedAndSavedSuccessfully ->
                            log.info("Download complete! Count: ${articlesDownloadedAndSavedSuccessfully.size}")
//                            log.info(
//                                    articlesDownloadedAndSavedSuccessfully
//                                            .joinToString(separator = "\n========###========\n") { it.urlRelative }
//                            )
//                            log.info("download complete")
                        },
                        onError = { log.error("Error while parseObjectsArticlesForLang", it) }
                )
    }

    @Async
    fun parseMostRecentArticlesForLang(
            lang: Lang,
            maxPageCount: Int? = null,
            processOnlyCount: Int? = null,
            innerArticlesDepth: Int? = null
    ) {
        downloadAndSaveAllRecentArticles(lang, maxPageCount, processOnlyCount, innerArticlesDepth)
                .subscribeBy(
                        onSuccess = { articlesDownloadedAndSavedSuccessfully ->
                            log.info("download complete")
                            log.info(
                                    articlesDownloadedAndSavedSuccessfully
                                            .joinToString(separator = "\n========###========\n") { it.urlRelative }
                            )
                            log.info("download complete")
                        },
                        onError = { log.error("Error while parseMostRecentArticlesForLang", it) }
                )
    }

    @Async
    fun parseMostRatedArticlesForLang(
            lang: Lang,
            totalPageCount: Int? = null,
            processOnlyCount: Int? = null,
            innerArticlesDepth: Int? = null
    ) {
        downloadAndSaveAllRatedArticles(lang, totalPageCount, processOnlyCount, innerArticlesDepth)
                .subscribeBy(
                        onSuccess = { articlesDownloadedAndSavedSuccessfully ->
                            log.info("download complete")
                            log.info(
                                    articlesDownloadedAndSavedSuccessfully
                                            .joinToString(separator = "\n========###========\n") { it.urlRelative }
                            )
                            log.info("download complete")
                        },
                        onError = {
                            log.error("Error while parseMostRatedArticlesForLang", it)
                        }
                )
    }

    @Async
    fun parseConcreteObjectArticlesForLang(
            objectsUrl: String,
            lang: Lang,
            offset: Int? = null,
            processOnlyCount: Int? = null,
            innerArticlesDepth: Int? = null
    ) {
        downloadAndSaveObjectArticles(lang, objectsUrl, offset, processOnlyCount, innerArticlesDepth)
                .subscribeBy(
                        onSuccess = { articlesDownloadedAndSavedSuccessfully ->
                            log.info("download complete")
                            log.info(
                                    articlesDownloadedAndSavedSuccessfully
                                            .joinToString(separator = "\n========###========\n") { it.urlRelative }
                            )
                            log.info("download complete")
                        },
                        onError = { log.error("Error while parseConcreteObjectArticlesForLang", it) }
                )
    }

    protected fun downloadAndSaveAllRatedArticles(
            lang: Lang,
            maxPageCount: Int? = null,
            processOnlyCount: Int? = null,
            innerArticlesDepth: Int? = null
    ): Single<List<ArticleForLang>> =
            with(BehaviorProcessor.createDefault(1)) {
                concatMap { page -> getRatedArticlesForLang(lang, page).toFlowable() }
                        .doOnNext { articles ->
                            if (articles.size != ScpReaderConstants.NUM_OF_ARTICLES_RATED_PAGE) {
                                onComplete()
                            } else {
                                if (maxPageCount == null || value!! < maxPageCount) {
                                    onNext(value!! + 1)
                                } else {
                                    onComplete()
                                }
                            }
                        }
//                        .doOnNext { log.info("articles size: ${it.size}") }
                        .toList()
                        .map { it.flatten() }
                        //test loading and save with less count of articles
                        .map { articlesToDownload ->
                            processOnlyCount?.let {
                                articlesToDownload.take(processOnlyCount)
                            } ?: articlesToDownload
                        }
                        .flatMap {
                            downloadAndSaveArticles(
                                    it,
                                    lang,
                                    innerArticlesDepth ?: DEFAULT_INNER_ARTICLES_DEPTH
                            )
                        }
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())

    protected fun downloadAndSaveAllRecentArticles(
            lang: Lang,
            maxPageCount: Int? = null,
            processOnlyCount: Int? = null,
            innerArticlesDepth: Int? = null
    ): Single<List<ArticleForLang>> {
        return getMostRecentArticlesPageCountForLang(lang)
                .flatMapPublisher { recentArticlesPagesCount ->
                    Flowable.range(1, maxPageCount ?: recentArticlesPagesCount)
                }
                .switchMap { page ->
                    getRecentArticlesForPage(lang, page).flatMapPublisher { Flowable.fromIterable(it) }
                }
                .toList()
                //test loading and save with less count of articles
                .map { articlesToDownload ->
                    processOnlyCount?.let {
                        articlesToDownload.take(processOnlyCount)
                    } ?: articlesToDownload
                }
                .flatMap { articles ->
                    downloadAndSaveArticles(articles, lang, innerArticlesDepth ?: DEFAULT_INNER_ARTICLES_DEPTH)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    private fun downloadAndSaveObjectArticles(
            lang: Lang,
            objectsUrl: String,
            offset: Int? = null,
            processOnlyCount: Int? = null,
            innerArticlesDepth: Int? = null
    ): Single<List<ArticleForLang>> =
            getObjectsArticlesForLang(lang, objectsUrl)
                    //test loading and save with less count of articles
                    .map { articlesToDownload ->
                        if (processOnlyCount == null && offset == null) {
                            articlesToDownload
                        } else {
                            var result = articlesToDownload
                            if (offset != null) {
                                result = articlesToDownload.subList(offset, articlesToDownload.size - 1)
                            }
                            if (processOnlyCount != null) {
                                result = result.take(processOnlyCount)
                            }
                            result
                        }
                    }
                    .flatMap { articlesToSave ->
                        downloadAndSaveArticles(
                                articlesToSave,
                                lang,
                                innerArticlesDepth ?: DEFAULT_INNER_ARTICLES_DEPTH
                        )
                    }
                    .doOnSuccess { articlesForLangInDb ->
                        // 1. get articleCategory by lang and objectUrl
                        // 2. delete articleToCategory relation for lang
                        // 3. save articleToCategory relation with order
                        val categoryToLang = categoryToLangService
                                .findByLangIdAndSiteUrl(lang.id, objectsUrl) ?: return@doOnSuccess
//                        log.info("categoryToLang: $categoryToLang")
                        var order = 0
                        val articlesForCategory = articlesForLangInDb
                                .map {
                                    ArticleCategoryForLangToArticleForLang(
                                            articleCategoryToLangId = categoryToLang.id!!,
                                            articleForLangId = it.id!!,
                                            orderInCategory = order++
                                    )
                                }

                        categoryToArticleService.updateCategoryForLangToArticleForLang(
                                categoryToLang.id!!,
                                articlesForCategory
                        )

                        val langEnum = ScpReaderConstants.Firebase.FirebaseInstance.valueOf(lang.id.toUpperCase())
                        val articlesToCategoryForCache = articleForLangService
                                .findAllArticlesForLangByArticleCategoryToLangId(categoryToLang.articleCategoryId)
                        cacheManager
                                .getCache(CATEGORIES_ARTICLES)
                                ?.put(
                                        SimpleKey(langEnum, categoryToLang.articleCategoryId),
                                        articlesToCategoryForCache
                                )
                    }
                    .doOnError { saveArticleParseError(lang.id, objectsUrl, it) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())

    fun getMostRecentArticlesPageCountForLang(lang: Lang): Single<Int> {
        return Single.create { subscriber ->
            val request = Request.Builder()
                    .url(lang.siteBaseUrlsToLangs?.first()?.siteBaseUrl + getRecentArticlesUrl())
                    .build()

            val responseBody = getResponseBody(request)

            val doc = Jsoup.parse(responseBody)

            //get num of pages
            val spanWithNumber = doc.getElementsByClass("pager-no").first()
            val text = spanWithNumber.text()
            val numOfPages = Integer.valueOf(text.substring(text.lastIndexOf(" ") + 1))

            subscriber.onSuccess(numOfPages)
        }
    }

    fun getRecentArticlesForPage(lang: Lang, page: Int): Single<List<ArticleForLang>> {
//        log.info("start request to: ${lang.siteBaseUrlsToLangs?.first()?.siteBaseUrl + getRecentArticlesUrl() + page}")
        return Single
                .create<List<ArticleForLang>> { subscriber ->
                    val request = Request.Builder()
                            .url(lang.siteBaseUrlsToLangs?.first()?.siteBaseUrl + getRecentArticlesUrl() + page)
                            .build()
                    val responseBody = getResponseBody(request)
                    val doc = Jsoup.parse(responseBody)
                    val articles = parseForRecentArticles(lang, doc)
                    subscriber.onSuccess(articles)
                }
                .doOnError {
                    saveArticleParseError(
                            lang.id,
                            lang.siteBaseUrlsToLangs?.first()?.siteBaseUrl + getRecentArticlesUrl() + page,
                            it
                    )
                }
    }

    fun getRatedArticlesForLang(lang: Lang, page: Int): Single<List<ArticleForLang>> {
        log.info("getRatedArticlesForLang: ${lang.langCode}, " +
                "url: ${lang.siteBaseUrlsToLangs?.first()?.siteBaseUrl}${getRatedArticlesUrl()}$page")
        return Single
                .create<List<ArticleForLang>> { subscriber ->
                    val request = Request.Builder()
                            .url(lang.siteBaseUrlsToLangs?.first()?.siteBaseUrl + getRatedArticlesUrl() + page)
                            .build()
                    val responseBody = getResponseBody(request)
                    val doc = Jsoup.parse(responseBody)
                    val articles = parseForRatedArticles(lang, doc)
                    subscriber.onSuccess(articles)
                }
                .doOnError {
                    saveArticleParseError(
                            lang.id,
                            lang.siteBaseUrlsToLangs?.first()?.siteBaseUrl + getRatedArticlesUrl() + page,
                            it
                    )
                }
    }

    @Suppress("DuplicatedCode")
    protected fun parseForRatedArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        log.info("start parsing rated articles for lang: $lang")
        val pageContent = doc.getElementById(ID_PAGE_CONTENT)
                ?: throw ScpParseException("$ID_PAGE_CONTENT not found!", NullPointerException())
        val listPagesBox = pageContent.getElementsByClass("list-pages-box").first()
                ?: throw ScpParseException("list-pages-box not found!", NullPointerException())
        val articles = mutableListOf<ArticleForLang>()
        val listOfElements = listPagesBox.getElementsByClass("list-pages-item")
        for (element in listOfElements) {
            val tagP = element.getElementsByTag(TAG_P).first()
            val tagA = tagP.getElementsByTag(TAG_A).first()
            val title = tagP.text().substring(0, tagP.text().indexOf(getArticleRatingStringDelimiter()))
            val url = tagA.attr(ATTR_HREF)
            //remove a tag to leave only text with rating
            tagA.remove()
            tagP.text(tagP.text().replace(", рейтинг ", ""))
            tagP.text(tagP.text().substring(0, tagP.text().length - 1))
            val rating = tagP.text().toInt()
            val article = ArticleForLang(
                    langId = lang.id,
                    urlRelative = lang.removeDomainFromUrl(url),
                    title = title,
                    rating = rating
            )
            articles.add(article)
        }
        return articles
    }

    fun getObjectsArticlesForLang(lang: Lang, objectsLink: String): Single<List<ArticleForLang>> {
//        log.info("getObjectsArticlesForLang: ${lang.langCode}, url: ${lang.siteBaseUrlsToLangs?.first()?.siteBaseUrl}$objectsLink")
        return Single.create { subscriber ->
            val request = Request.Builder()
                    .url(lang.siteBaseUrlsToLangs?.first()?.siteBaseUrl + objectsLink)
                    .build()
            val responseBody = try {
                val response: Response = okHttpClient.newCall(request).execute()
                val body = response.body()
                if (body != null) {
                    body.string()
                } else {
                    subscriber.onError(ScpParseException("Body is null!", NullPointerException()))
                    return@create
                }
            } catch (e: IOException) {
                subscriber.onError(IOException("Connection error in getObjectsArticlesForLang: ${lang.id}, $objectsLink!"))
                return@create
            }
            try {
                val doc = Jsoup.parse(responseBody)
                val articles = parseForObjectArticles(lang, doc)
                subscriber.onSuccess(articles)
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
    }

    @Suppress("DuplicatedCode")
    protected fun parseForObjectArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        val pageContent = doc.getElementById(ID_PAGE_CONTENT)
                ?: throw ScpParseException("$ID_PAGE_CONTENT not found!", NullPointerException())
        //parse
        val listPagesBox = pageContent.getElementsByClass("list-pages-box").first()
        listPagesBox?.remove()
        val collapsibleBlock = pageContent.getElementsByClass(CLASS_SPOILER).first()
        collapsibleBlock?.remove()
        val table = pageContent.getElementsByTag(TAG_TABLE).first()
        table?.remove()
        val h2 = doc.getElementById("toc0")
        h2?.remove()
        //now we will remove all html code before tag h2,with id toc1
        var allHtml = pageContent.html()
        val indexOfh2WithIdToc1 = allHtml.indexOf("<h2 id=\"toc1\">")
        var indexOfHr = allHtml.indexOf("<hr>")
        //for other objects filials there is no HR tag at the end...
        if (indexOfHr < indexOfh2WithIdToc1) {
            indexOfHr = allHtml.indexOf("<p style=\"text-align: center;\">= = = =</p>")
        }
        if (indexOfHr < indexOfh2WithIdToc1) {
            indexOfHr = allHtml.length
        }
        allHtml = allHtml.substring(indexOfh2WithIdToc1, indexOfHr)
        val document = Jsoup.parse(allHtml)
        val h2withIdToc1 = document.getElementById("toc1")
        h2withIdToc1.remove()
        val allH2Tags: Elements = document.getElementsByTag("h2")
        for (h2Tag in allH2Tags) {
            val brTag = Element(Tag.valueOf("br"), "")
            h2Tag.replaceWith(brTag)
        }
        val allArticles = document.getElementsByTag(TAG_BODY).first().html()
        val arrayOfArticles = allArticles.split("<br>").toTypedArray()
        val articles: MutableList<ArticleForLang> = mutableListOf()
        for (arrayItem in arrayOfArticles) {
            if (TextUtils.isEmpty(arrayItem.trim())) {
                continue
            }
            val arrayItemParsed = Jsoup.parse(arrayItem)
            //type of object
            val imageURL = arrayItemParsed.getElementsByTag(TAG_IMG).first().attr(ATTR_SRC)
            val type = getObjectTypeByImageUrl(imageURL)
            val url = arrayItemParsed.getElementsByTag(TAG_A).first().attr(ATTR_HREF)
            val title = arrayItemParsed.text()
            val article = ArticleForLang(
                    langId = lang.id,
                    urlRelative = lang.removeDomainFromUrl(url),
                    title = title,
                    articleTypeEnumEnumValue = type
            )
            articles.add(article)
        }
        return articles
    }

    protected fun downloadAndSaveArticles(
            articlesToDownload: List<ArticleForLang>,
            lang: Lang,
            innerArticlesDepth: Int = 0
    ): Single<List<ArticleForLang>> {
//        log.info("downloadAndSaveArticles articles size: ${articlesToDownload.size}")
        return Single.just(articlesToDownload)
                .map { articles ->
                    articles.mapNotNull { articleToDownload ->
                        try {
                            saveArticle(
                                    articleToDownload,
                                    lang,
                                    innerArticlesDepth
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    /**
     * @throws ScpParseException
     */
    @Transactional
    protected fun saveArticle(
            articleToSave: ArticleForLang,
            lang: Lang,
            innerArticlesDepth: Int = 0,
            printTextParts: Boolean = false
    ): ArticleForLang {
//        log.info("parse article: ${articleToSave.urlRelative}")
        try {
            val category = categoryToLangService.findByLangIdAndSiteUrl(lang.id, articleToSave.urlRelative)
            val relativeUrlToCheck = lang.removeDomainFromUrl(articleToSave.urlRelative)
            if (category != null || getRecentArticlesUrl() == relativeUrlToCheck || getRatedArticlesUrl() == relativeUrlToCheck) {
                throw IllegalStateException("Attempt to parse category as article")
            }

            val articleDownloaded = getArticleFromApi(articleToSave.urlRelative, lang, printTextParts)

            val articleInDb = try {
                articleService.getArticleByUrlRelative(articleDownloaded.urlRelative)
                        ?: articleService.save(Article())
            } catch (e: IncorrectResultSizeDataAccessException) {
                val articles = articleService.getArticlesByUrlRelative(articleDownloaded.urlRelative)
                val articleIdsToDelete = articles.subList(0, articles.size - 1).map { it.id!! }
                val articlesForLangsIdsToDelete = articleForLangService.findIdsByArticleIds(articleIdsToDelete)
                articleForLangService.deleteByIds(articlesForLangsIdsToDelete)
                articles.last()
            }

            var articleForLangInDb = articleForLangService
                    .getArticleForLangByUrlRelativeAndLang(
                            articleDownloaded.urlRelative,
                            lang.id
                    )

            if (articleForLangInDb == null) {
                articleForLangInDb = articleForLangService.save(
                        articleDownloaded.apply {
                            articleId = articleInDb.id
                            createdOnSite = articleToSave.createdOnSite
                            updatedOnSite = articleToSave.updatedOnSite
                        }
                )
            } else {
                articleForLangInDb = articleForLangService.save(
                        articleForLangInDb.apply {
                            commentsUrl = articleDownloaded.commentsUrl
                            rating = articleDownloaded.rating
                            title = articleDownloaded.title
                            text = articleDownloaded.text
                            createdOnSite = articleToSave.createdOnSite
                            updatedOnSite = articleToSave.updatedOnSite
                        }
                )
            }

            //set keys for article images.
            val imagesToSave = articleDownloaded.images.map {
                it.apply { articleForLangId = articleForLangInDb.id!! }
            }
            articlesImagesService.save(imagesToSave)

            articleToSave.articleTypeEnumEnumValue?.let {
                manageArticleType(it, articleForLangInDb)
            }

            manageTagsForArticle(lang.id, articleDownloaded, articleForLangInDb)

            manageArticleTextParts(articleDownloaded, articleForLangInDb)

            //parse inner
            getAndSaveInnerArticles(lang, articleDownloaded, innerArticlesDepth)

            createArticleToArticleRelation(articleDownloaded, articleForLangInDb.id!!, lang)

            val langEnum =
                    ScpReaderConstants.Firebase.FirebaseInstance.valueOf(lang.id.toUpperCase())
            val articleWithTextForCache = articleForLangService.getOneByIdAsDto(articleForLangInDb.id!!)
            cacheManager
                    .getCache(ARTICLE_TO_LANG_DTO_BY_URL_RELATIVE_AND_LANG)
                    ?.put(SimpleKey(langEnum, articleForLangInDb.urlRelative), articleWithTextForCache)
            cacheManager
                    .getCache(ARTICLE_TO_LANG_DTO_BY_ID)
                    ?.put(SimpleKey(articleForLangInDb.id), articleWithTextForCache)

            return articleForLangInDb
        } catch (e: Exception) {
            log.error("Error in article parsing: ${articleToSave.urlRelative}, lang: ${lang.id}", e)

            saveArticleParseError(articleToSave.langId, articleToSave.urlRelative, e)

            throw e
        }
    }

    private fun saveArticleParseError(langId: String, url: String, e: Throwable) {
        val error = ArticleParseError(
                langId = langId,
                urlRelative = url,
                errorClass = e::class.java.simpleName,
                errorMessage = e.message,
                stacktrace = errorUtils.stackTraceAsString(e)!!
        )

        val cause = e.cause
        if (cause != null) {
            val causeErrorClass = cause::class.java.simpleName
            val causeErrorMessage = cause.message

            error.causeErrorClass = causeErrorClass
            error.causeErrorMessage = causeErrorMessage
            error.causeStacktrace = errorUtils.stackTraceAsString(cause)
        }

        articleParseErrorService.save(error)
    }

    private fun manageArticleTextParts(
            articleDownloaded: ArticleForLang,
            articleForLangInDb: ArticleForLang
    ) {
        val textPartsToSave = articleDownloaded.textParts ?: return
        if (textPartsToSave.isNotEmpty()) {
            //clear textPartsInDB before write
            textPartService.deleteByArticleToLangId(articleForLangInDb.id!!)
            //todo save in one insert request
            textPartsToSave.forEach { saveTextPart(it, articleForLangInDb.id, null) }
        }
    }

    private fun saveTextPart(textPart: TextPart, articleToLangId: Long, parentId: Long?) {
        textPart.articleToLangId = articleToLangId
        textPart.parentId = parentId
        val savedTextPart = textPartService.insert(textPart)
        textPart.innerTextParts?.forEach { saveTextPart(it, articleToLangId, savedTextPart.id) }
    }

    @Suppress("DuplicatedCode")
    protected fun parseForRecentArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        val table = doc.getElementsByClass("wiki-content-table").first()
                ?: throw ScpParseException("Can't find element for class \"wiki-content-table\"", NullPointerException())

        val articles = mutableListOf<ArticleForLang>()
        val listOfElements = table.getElementsByTag("tr")
        for (i in 1/*start from 1 as first row is tables header*/ until listOfElements.size) {
            val tableRow = listOfElements[i]
            val listOfTd = tableRow.getElementsByTag("td")
            //title and url
            val firstTd = listOfTd.first()
            val tagA = firstTd.getElementsByTag(TAG_A).first()
            val title = tagA.text()
            val url = tagA.attr(ATTR_HREF)
            //rating
            val rating = Integer.parseInt(listOfTd[1].text())

            val createdDate = listOfTd[3].text().trim()
            val updatedDate = listOfTd[4].text().trim()

            val article = ArticleForLang(
                    langId = lang.id,
                    urlRelative = lang.removeDomainFromUrl(url),
                    title = title,
                    rating = rating,
                    createdOnSite = Timestamp(getDateFormatForLang().parse(createdDate).time),
                    updatedOnSite = Timestamp(getDateFormatForLang().parse(updatedDate).time)
            )
            articles.add(article)
        }

        return articles
    }

    @Async
    fun parseArticleForLang(
            urlRelative: String,
            lang: Lang,
            innerArticlesDepth: Int = 0,
            printTextParts: Boolean = false
    ) {
        val savedArticle = saveArticle(
                ArticleForLang(
                        urlRelative = urlRelative,
                        langId = lang.id
                ),
                lang,
                innerArticlesDepth = innerArticlesDepth,
                printTextParts = printTextParts
        )
        log.info("Article saved. id: ${savedArticle.id}, articleId: ${savedArticle.articleId}")
    }

    fun parseArticleForLangSync(
            urlRelative: String,
            lang: Lang,
            innerArticlesDepth: Int = 0,
            printTextParts: Boolean = false
    ): ArticleForLang? {
        val savedArticle = saveArticle(
                ArticleForLang(
                        urlRelative = urlRelative,
                        langId = lang.id
                ),
                lang,
                innerArticlesDepth = innerArticlesDepth,
                printTextParts = printTextParts
        )
        log.info("Article saved. id: ${savedArticle.id}, articleId: ${savedArticle.articleId}")

        return savedArticle
    }

    /**
     * @throws ScpParseException
     */
    private fun getArticleFromApi(url: String, lang: Lang, printTextParts: Boolean = false): ArticleForLang {
        val request = Request.Builder()
                .url(lang.siteBaseUrlsToLangs?.first()?.siteBaseUrl + url)
                .build()

        val response = okHttpClient.newCall(request).execute()
        var responseBody = response.body()?.string()
                ?: throw ScpParseException("Response body is NULL for url: $url", NullPointerException())

        //remove all fucking RTL(&lrm) used for text-alignment. What a fucking idiots!..
        responseBody = responseBody.replace("[\\p{Cc}\\p{Cf}]".toRegex(), "")

        val doc = Jsoup.parse(responseBody)
        val pageContent = getArticlePageContentTag(doc)
                ?: throw ScpParseException("pageContent is NULL for url: $url", NullPointerException())
        val p404 = pageContent.getElementById("404-message")
        if (p404 != null || response.code() == HttpStatus.NOT_FOUND.value()) {
            throw ScpParseException("404 page for url: $url")
        } else {
            return parseArticleService.parseArticle(url, doc, pageContent, lang, printTextParts)
        }
    }

    /**
     * We need this as in FR site all article content wrapped in another div... ***!!!11
     *
     * @return Element with article content
     */
    protected fun getArticlePageContentTag(doc: Document): Element? =
            doc.getElementById(ID_PAGE_CONTENT)

    private fun createArticleToArticleRelation(
            articleDownloaded: ArticleForLang,
            articleDownloadedId: Long,
            lang: Lang
    ) {
        //create links for relation ArticleForLang -> ArticleForLang
        val articlesToArticle = articleDownloaded
                .innerArticlesForLang
                //remove nulls and map to articleForLang to get it's id
                .mapNotNull { articleForLangService.getIdByUrlRelativeAndLangId(it.urlRelative, lang.id) }
                .map {
                    ArticleForLangToArticleForLang(
                            parentArticleForLangId = articleDownloadedId,
                            articleForLangId = it
                    )
                }
                .filter {
                    articleForLangToArticleForLangService
                            .findByParentArticleForLangIdAndArticleForLangId(
                                    it.parentArticleForLangId, it.articleForLangId
                            ) == null
                }

        if (articlesToArticle.isNotEmpty()) {
            articleForLangToArticleForLangService.insert(articlesToArticle)
        }
    }

    private fun manageTagsForArticle(
            langId: String,
            articleDownloaded: ArticleForLang,
            articleForLangInDb: ArticleForLang
    ) {
        //save tagsForLang if not exists
        val tagsForLang = articleDownloaded.tags.map {
            tagForLangService.findOneByLangIdAndTitle(langId, it.title)
                    ?: kotlin.run {
                        val tag = tagService.insert(ArticleTag())

                        val tagToLang = TagForLang(
                                title = it.title,
                                langId = langId,
                                tagId = tag.id!!
                        )
                        tagForLangService.insert(tagToLang)
                    }
        }

        //save tagsForArticleForLang
        tagsForLang.forEach {
            tagForArticleForLangService.getOneByTagForLangIdAndArticleForLangIdOrCreate(
                    tagForLangId = it.id!!,
                    articleForLangId = articleForLangInDb.id!!
            )
        }

        //do not insert in Tag, as we'll do it manually later. Maybe...
    }

    private fun manageArticleType(
            articleTypeEnum: ScpReaderConstants.ArticleTypeEnum,
            articleForLangInDb: ArticleForLang
    ) {
        val articleType = articleTypeService.getByEnumValue(articleTypeEnum)
        //create article connection to article_type
        //or check if types not equal and update
        val articleToArticleTypeInDb = articleAndArticleTypeService.getByArticleId(articleForLangInDb.articleId!!)
        if (articleToArticleTypeInDb == null) {
            articleAndArticleTypeService.save(
                    ArticlesAndArticleTypes(
                            articleId = articleForLangInDb.articleId!!,
                            articleTypeId = articleType.id!!
                    )
            )
        } else if (articleToArticleTypeInDb.articleTypeId != articleType.id) {
            log.info("""
                    |Article types not equal!
                    | Article id: ${articleForLangInDb.articleId}, 
                    | ArticleForLangId: ${articleForLangInDb.id},
                    | ArticleForLang urlRelative: ${articleForLangInDb.urlRelative},
                    | Saved typeId: ${articleToArticleTypeInDb.articleTypeId},
                    | New typeId: ${articleType.id}
                    """.trimMargin()
            )
            articleAndArticleTypeService.save(articleToArticleTypeInDb.apply { articleTypeId = articleType.id!! })
        }
    }

    private fun getAndSaveInnerArticles(
            lang: Lang,
            articleDownloaded: ArticleForLang,
            maxDepth: Int = 0,
            currentDepthLevel: Int = 0
    ) {
        if (currentDepthLevel >= maxDepth) {
            return
        }
        log.info("getAndSaveInnerArticles: ${articleDownloaded.title}, $currentDepthLevel")

        val innerArticlesUrls = articleDownloaded.innerArticlesForLang.map { it.urlRelative }
        for (innerUrl in innerArticlesUrls) {
            log.info("save inner article: $innerUrl")
            try {
                val articleForLang = ArticleForLang(urlRelative = innerUrl, langId = lang.id)
                val innerArticleDownloaded = saveArticle(articleForLang, lang)

                getAndSaveInnerArticles(lang, innerArticleDownloaded, maxDepth, currentDepthLevel + 1)
            } catch (e: Exception) {
                log.error("error while save inner article for url: $innerUrl", e)
            }
        }
    }

    private fun getResponseBody(request: Request): String {
        val response = okHttpClient.newCall(request).execute()
        val body = response.body()
        if (body != null) {
            return body.string()
        } else {
            throw ScpParseException("body is null!", NullPointerException())
        }
    }

    protected fun getObjectTypeByImageUrl(imageURL: String): ScpReaderConstants.ArticleTypeEnum {
        val typeEnum: ScpReaderConstants.ArticleTypeEnum

        when (imageURL) {
            "http://scp-ru.wdfiles.com/local--files/scp-list-5/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-5/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-4/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-4/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-3/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-3/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-2/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-2/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-ru/na(1).png",
            "http://scp-ru.wdfiles.com/local--files/scp-list/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series/na.png",
            "http://scp-ru.wdfiles.com/local--files/archive/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-j/na(1).png",
                //other filials
            "http://scp-ru.wdfiles.com/local--files/scp-list-fr/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-jp/safe1.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-es/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-pl/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-de/safe1.png" ->
                typeEnum = ScpReaderConstants.ArticleTypeEnum.NEUTRAL_OR_NOT_ADDED

            "http://scp-ru.wdfiles.com/local--files/scp-list-5/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-5/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-4/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-4/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-3/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-3/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-2/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-2/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-ru/safe(1).png",
            "http://scp-ru.wdfiles.com/local--files/scp-list/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series/safe.png",
            "http://scp-ru.wdfiles.com/local--files/archive/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-j/safe(1).png",
                //other filials
            "http://scp-ru.wdfiles.com/local--files/scp-list-fr/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-jp/na1.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-es/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-pl/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-de/na1.png" ->
                typeEnum = ScpReaderConstants.ArticleTypeEnum.SAFE

            "http://scp-ru.wdfiles.com/local--files/scp-list-5/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-5/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-4/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-4/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-3/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-3/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-2/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-2/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-ru/euclid(1).png",
            "http://scp-ru.wdfiles.com/local--files/scp-list/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/archive/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-j/euclid(1).png",
                //other filials
            "http://scp-ru.wdfiles.com/local--files/scp-list-fr/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-jp/euclid1.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-es/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-pl/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-de/euclid1.png" ->
                typeEnum = ScpReaderConstants.ArticleTypeEnum.EUCLID

            "http://scp-ru.wdfiles.com/local--files/scp-list-5/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-5/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-4/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-4/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-3/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-3/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-2/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-2/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-ru/keter(1).png",
            "http://scp-ru.wdfiles.com/local--files/scp-list/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series/keter.png",
            "http://scp-ru.wdfiles.com/local--files/archive/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-j/keter(1).png",
                //other filials
            "http://scp-ru.wdfiles.com/local--files/scp-list-fr/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-jp/keter1.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-es/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-pl/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-de/keter1.png" ->
                typeEnum = ScpReaderConstants.ArticleTypeEnum.KETER

            "http://scp-ru.wdfiles.com/local--files/scp-list-5/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-5/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-4/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-4/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-3/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-3/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-2/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-2/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-ru/thaumiel(1).png",
            "http://scp-ru.wdfiles.com/local--files/scp-series-ru/thaumiel(1).png",
            "http://scp-ru.wdfiles.com/local--files/scp-list/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-series/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/archive/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-j/thaumiel(1).png",
                //other filials
            "http://scp-ru.wdfiles.com/local--files/scp-list-fr/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-jp/thaumiel1.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-es/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-pl/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-de/thaumiel1.png" ->
                typeEnum = ScpReaderConstants.ArticleTypeEnum.THAUMIEL

            else -> typeEnum = ScpReaderConstants.ArticleTypeEnum.NONE
        }
        return typeEnum
    }

    companion object {
        private const val DATE_FORMAT_PATTERN_EN = "dd MMM yyyy HH:mm"

        fun getDateFormatForLang() = SimpleDateFormat(DATE_FORMAT_PATTERN_EN, Locale.ENGLISH)

        const val DEFAULT_INNER_ARTICLES_DEPTH = 0
    }
}

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class ScpParseException(
        override val message: String?,
        override val cause: Throwable? = null
) : RuntimeException(message, cause)
