package ru.kuchanov.scpreaderapi.service.parse

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.rxkotlin.subscribeBy
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.annotation.Primary
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.Article
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLangToArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.configuration.NetworkConfiguration
import ru.kuchanov.scpreaderapi.service.article.*
import ru.kuchanov.scpreaderapi.service.article.tags.TagForArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.tags.TagForLangService
import java.io.IOException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


@Primary
@Service
class ArticleParsingServiceBase {

    @Autowired
    private lateinit var autowireCapableBeanFactory: AutowireCapableBeanFactory

    @Autowired
    @Qualifier(NetworkConfiguration.QUALIFIER_OK_HTTP_CLIENT_NOT_LOGGING)
    protected lateinit var okHttpClient: OkHttpClient

    @Autowired
    private lateinit var parseHtmlService: ParseHtmlService

    @Autowired
    private lateinit var articleService: ArticleService

    @Autowired
    private lateinit var articleForLangService: ArticleForLangService

    @Autowired
    private lateinit var articleForLangToArticleForLangService: ArticleForLangToArticleForLangService

//    @Autowired
//    private lateinit var articlesImagesService: ArticlesImagesService

    @Autowired
    private lateinit var tagForArticleForLangService: TagForArticleForLangService

    @Autowired
    private lateinit var tagForLangService: TagForLangService

    fun getParsingRealizationForLang(lang: Lang): ArticleParsingServiceBase =
            when (lang.langCode) {
                ScpReaderConstants.Firebase.FirebaseInstance.RU.lang -> autowireCapableBeanFactory.getBean(ArticleParsingServiceImplRU::class.java)
                ScpReaderConstants.Firebase.FirebaseInstance.EN.lang -> autowireCapableBeanFactory.getBean(ArticleParsingServiceImplEN::class.java)
                ScpReaderConstants.Firebase.FirebaseInstance.DE.lang -> autowireCapableBeanFactory.getBean(ArticleParsingServiceImplDE::class.java)
                ScpReaderConstants.Firebase.FirebaseInstance.FR.lang -> autowireCapableBeanFactory.getBean(ArticleParsingServiceImplFR::class.java)
                ScpReaderConstants.Firebase.FirebaseInstance.ES.lang -> autowireCapableBeanFactory.getBean(ArticleParsingServiceImplES::class.java)
                ScpReaderConstants.Firebase.FirebaseInstance.IT.lang -> autowireCapableBeanFactory.getBean(ArticleParsingServiceImplIT::class.java)
                ScpReaderConstants.Firebase.FirebaseInstance.PL.lang -> autowireCapableBeanFactory.getBean(ArticleParsingServiceImplPL::class.java)
                ScpReaderConstants.Firebase.FirebaseInstance.PT.lang -> autowireCapableBeanFactory.getBean(ArticleParsingServiceImplPT::class.java)
                "cn" -> autowireCapableBeanFactory.getBean(ArticleParsingServiceImplCH::class.java)
                else -> throw NotImplementedError("No parsing realization, need lang(current lang: $lang)")
            }

    fun getRatedArticlesUrl() = "/top-rated-pages/p/"

    fun getRecentArticlesUrl() = "/most-recently-created/p/"

    fun getObjectArticlesUrls() = listOf(
            "/scp-list",
            "/scp-list-2",
            "/scp-list-3",
            "/scp-list-4",
            "/scp-list-5"
    )

    fun getArticleRatingStringDelimiter() = ", рейтинг"

    fun getArticleRatingStringDelimiterEnd() = ""

    @Async
    fun parseMostRecentArticlesForLang(
            lang: Lang,
            maxPageCount: Int? = null,
            processOnlyCount: Int? = null
    ) {
        getMostRecentArticlesPageCountForLang(lang)
                .flatMapObservable { recentArticlesPagesCount ->
                    Observable.range(1, maxPageCount ?: recentArticlesPagesCount)
                }
                .flatMap { page ->
                    getRecentArticlesForPage(lang, page)
                            .flatMapObservable { Observable.fromIterable(it) }
                }
                .toList()
                //test loading and save with less count of articles
                .map { articlesToDownload ->
                    processOnlyCount?.let {
                        articlesToDownload.take(processOnlyCount)
                    } ?: articlesToDownload
                }
                .flatMap { downloadAndSaveArticles(it, lang, DEFAULT_INNER_ARTICLES_DEPTH) }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = { articlesDownloadedAndSavedSuccessfully ->
                            println("download complete")
                            println(
                                    articlesDownloadedAndSavedSuccessfully
                                            .map { it?.urlRelative }
                                            .joinToString(separator = "\n========###========\n")
                            )
                            println("download complete")
                        },
                        onError = {
                            it.printStackTrace()
                        }
                )
    }

    @Async
    fun parseMostRatedArticlesForLang(
            lang: Lang,
            totalPageCount: Int? = null,
            processOnlyCount: Int? = null
    ) {
        val subject = BehaviorProcessor.createDefault(1)

        subject
                .concatMap { page -> getRatedArticlesForLang(lang, page).toFlowable() }
                .doOnNext { articles ->
                    if (articles.size != ScpReaderConstants.NUM_OF_ARTICLES_RATED_PAGE) {
                        subject.onComplete()
                    } else {
                        if (totalPageCount == null || subject.value!! < totalPageCount) {
                            subject.onNext(subject.value!! + 1)
                        } else {
                            subject.onComplete()
                        }

                    }
                }
                .doOnNext { println("articles size: ${it.size}") }
                .toList()
                .map { it.flatten() }

                //test loading and save with less count of articles
                .map { articlesToDownload ->
                    processOnlyCount?.let {
                        articlesToDownload.take(processOnlyCount)
                    } ?: articlesToDownload
                }
                .flatMap { downloadAndSaveArticles(it, lang, DEFAULT_INNER_ARTICLES_DEPTH) }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = { articlesDownloadedAndSavedSuccessfully ->
                            println("download complete")
                            println(
                                    articlesDownloadedAndSavedSuccessfully
                                            .map { it?.urlRelative }
                                            .joinToString(separator = "\n========###========\n")
                            )
                            println("download complete")
                        },
                        onError = {
                            it.printStackTrace()
                        }
                )
    }

    @Async
    fun parseObjectsArticlesForLang(
            lang: Lang,
            totalPageCount: Int? = null,
            processOnlyCount: Int? = null
    ) {
        Flowable.fromIterable(getObjectArticlesUrls())
                .flatMapSingle { url -> getObjectsArticlesForLang(lang, url) }
                .toList()
                .map { it.flatten() }

                //test loading and save with less count of articles
                .map { articlesToDownload ->
                    processOnlyCount?.let {
                        articlesToDownload.take(processOnlyCount)
                    } ?: articlesToDownload
                }
                .flatMap { downloadAndSaveArticles(it, lang, DEFAULT_INNER_ARTICLES_DEPTH) }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = { articlesDownloadedAndSavedSuccessfully ->
                            println("download complete")
                            println(
                                    articlesDownloadedAndSavedSuccessfully
                                            .map { it?.urlRelative }
                                            .joinToString(separator = "\n========###========\n")
                            )
                            println("download complete")
                        },
                        onError = {
                            it.printStackTrace()
                        }
                )
    }

    fun getMostRecentArticlesPageCountForLang(lang: Lang): Single<Int> {
        return Single.create<Int> { subscriber ->
            val request = Request.Builder()
                    .url(lang.siteBaseUrl + getRecentArticlesUrl())
                    .build()

            val responseBody: String
            try {
                val response = okHttpClient.newCall(request).execute()
                val body = response.body()
                if (body != null) {
                    responseBody = body.string()
                } else {
                    subscriber.onError(ScpParseException("error while getRecentArticlesPageCountForLang"))
                    return@create
                }
            } catch (e: IOException) {
                subscriber.onError(IOException("error while getRecentArticlesPageCountForLang", e))
                return@create
            }

            try {
                val doc = Jsoup.parse(responseBody)

                //get num of pages
                val spanWithNumber = doc.getElementsByClass("pager-no").first()
                val text = spanWithNumber.text()
                val numOfPages = Integer.valueOf(text.substring(text.lastIndexOf(" ") + 1))

                subscriber.onSuccess(numOfPages)
            } catch (e: Exception) {
                println("error while get arts list")
                subscriber.onError(e)
            }
        }
    }

    fun getRecentArticlesForPage(lang: Lang, page: Int): Single<List<ArticleForLang>> {
        return Single.create<List<ArticleForLang>> {

            val request = Request.Builder()
                    .url(lang.siteBaseUrl + getRecentArticlesUrl() + page)
                    .build()

            println("start request to: ${lang.siteBaseUrl + getRecentArticlesUrl() + page}")

            val responseBody = okHttpClient
                    .newCall(request)
                    .execute()
                    .body()
                    ?.string()
                    ?: throw IOException("error while getRecentArticlesForPage: $page")
            val doc: Document = Jsoup.parse(responseBody)

            val articles = parseForRecentArticles(lang, doc)

            it.onSuccess(articles)
        }
    }

    fun getRatedArticlesForLang(lang: Lang, page: Int): Single<List<ArticleForLang>> {
        return Single.create { subscriber: SingleEmitter<List<ArticleForLang>> ->

            val request: Request = Request.Builder()
                    .url(lang.siteBaseUrl + getRatedArticlesUrl() + page)
                    .build()
            val responseBody: String
            responseBody = try {
                val response: Response = okHttpClient.newCall(request).execute()
                val body = response.body()
                if (body != null) {
                    body.string()
                } else {
                    subscriber.onError(ScpParseException("parse error getRatedArticlesForLang!"))
                    return@create
                }
            } catch (e: IOException) {
                subscriber.onError(IOException("connection error!"))
                return@create
            }
            try {
                val doc = Jsoup.parse(responseBody)
                val articles: List<ArticleForLang> = parseForRatedArticles(lang, doc)
                subscriber.onSuccess(articles)
            } catch (e: Exception) {
                println("error while get arts list")
                subscriber.onError(e)
            } catch (e: ScpParseException) {
                println("error while get arts list")
                subscriber.onError(e)
            }
        }
    }

    @Suppress("DuplicatedCode")
    protected fun parseForRatedArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        println("start parsing rated articles for lang: $lang")
        val pageContent = doc.getElementById("page-content")
                ?: throw ScpParseException("parse error!")
        val listPagesBox = pageContent.getElementsByClass("list-pages-box").first()
                ?: throw ScpParseException("parse error!")
        val articles = mutableListOf<ArticleForLang>()
        val listOfElements = listPagesBox.getElementsByClass("list-pages-item")
        for (element in listOfElements) {
            val tagP = element.getElementsByTag("p").first()
            val tagA = tagP.getElementsByTag("a").first()
            val title = tagP.text().substring(0, tagP.text().indexOf(getArticleRatingStringDelimiter()))
            val url: String = lang.siteBaseUrl + tagA.attr("href")
            //remove a tag to leave only text with rating
            tagA.remove()
            tagP.text(tagP.text().replace(", рейтинг ", ""))
            tagP.text(tagP.text().substring(0, tagP.text().length - 1))
            val rating = tagP.text().toInt()
            val article = ArticleForLang(
                    langId = lang.id,
                    urlRelative = url.replace(lang.siteBaseUrl, "").trim(),
                    title = title,
                    rating = rating
            )
            articles.add(article)
        }
        return articles
    }

    fun getObjectsArticlesForLang(lang: Lang, objectsLink: String): Single<List<ArticleForLang>> {
        println("getObjectsArticlesForLang: ${lang.langCode}, $objectsLink")
        return Single.create { subscriber ->
            val request = Request.Builder()
                    .url(lang.siteBaseUrl + objectsLink)
                    .build()
            val responseBody: String
            responseBody = try {
                val response: Response = okHttpClient.newCall(request).execute()
                val body = response.body()
                if (body != null) {
                    body.string()
                } else {
                    subscriber.onError(ScpParseException("parse error!"))
                    return@create
                }
            } catch (e: IOException) {
                subscriber.onError(IOException("connection error!"))
                return@create
            }
            try {
                val doc = Jsoup.parse(responseBody)
                val articles = parseForObjectArticles(lang, doc)
                subscriber.onSuccess(articles)
            } catch (e: Exception) {
                println("error while get arts list")
                subscriber.onError(e)
            } catch (e: ScpParseException) {
                println("error while get arts list")
                subscriber.onError(e)
            }
        }
    }

    protected fun parseForObjectArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        val pageContent = doc.getElementById("page-content")
                ?: throw ScpParseException("parse error!")
        //parse
        val listPagesBox = pageContent.getElementsByClass("list-pages-box").first()
        listPagesBox?.remove()
        val collapsibleBlock = pageContent.getElementsByClass("collapsible-block").first()
        collapsibleBlock?.remove()
        val table = pageContent.getElementsByTag("table").first()
        table?.remove()
        val h2 = doc.getElementById("toc0")
        h2?.remove()
        //now we will remove all html code before tag h2,with id toc1
        var allHtml = pageContent.html()
        val indexOfh2WithIdToc1 = allHtml.indexOf("<h2 id=\"toc1\">")
        var indexOfhr = allHtml.indexOf("<hr>")
        //for other objects filials there is no HR tag at the end...
        if (indexOfhr < indexOfh2WithIdToc1) {
            indexOfhr = allHtml.indexOf("<p style=\"text-align: center;\">= = = =</p>")
        }
        if (indexOfhr < indexOfh2WithIdToc1) {
            indexOfhr = allHtml.length
        }
        allHtml = allHtml.substring(indexOfh2WithIdToc1, indexOfhr)
        val document = Jsoup.parse(allHtml)
        val h2withIdToc1 = document.getElementById("toc1")
        h2withIdToc1.remove()
        val allh2Tags: Elements = document.getElementsByTag("h2")
        for (h2Tag in allh2Tags) {
            val brTag = Element(Tag.valueOf("br"), "")
            h2Tag.replaceWith(brTag)
        }
        val allArticles = document.getElementsByTag("body").first().html()
        val arrayOfArticles = allArticles.split("<br>").toTypedArray()
        val articles: MutableList<ArticleForLang> = ArrayList()
        for (arrayItem in arrayOfArticles) {
            if (TextUtils.isEmpty(arrayItem.trim())) {
                continue
            }
//            println("arrayItem: $arrayItem")
            val arrayItemParsed = Jsoup.parse(arrayItem)
//            println("arrayItemParsed: $arrayItemParsed")
            //type of object
            val imageURL = arrayItemParsed.getElementsByTag("img").first().attr("src")
            //TODO do something with obj type like migrate new column do db
            val type = getObjectTypeByImageUrl(imageURL)
            val url: String = lang.siteBaseUrl + arrayItemParsed.getElementsByTag("a").first().attr("href")
            val title = arrayItemParsed.text()
            val article = ArticleForLang(
                    langId = lang.id,
                    urlRelative = url.replace(lang.siteBaseUrl, "").trim(),
                    title = title

            )
            articles.add(article)
        }
        return articles
    }

    protected fun downloadAndSaveArticles(
            articlesToDownload: List<ArticleForLang>,
            lang: Lang,
            innerArticlesDepth: Int = 0
    ): Single<List<ArticleForLang?>> {
        return Single.just(articlesToDownload)
                .map { articles ->
                    articles.map { articleToDownload ->
                        saveArticle(
                                lang,
                                articleToDownload.urlRelative,
                                innerArticlesDepth,
                                articleToDownload.createdOnSite,
                                articleToDownload.updatedOnSite
                        )
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    protected fun saveArticle(
            lang: Lang,
            urlRelative: String,
            innerArticlesDepth: Int = 0,
            createdOnSite: Timestamp? = null,
            updatedOnSite: Timestamp? = null
    ): ArticleForLang? {
        try {
            val articleDownloaded = getArticleFromApi(urlRelative, lang)

            if (articleDownloaded != null) {
                var articleInDb = articleService.getArticleByUrlRelative(articleDownloaded.urlRelative)
                if (articleInDb == null) {
                    articleInDb = articleService.insert(Article())
                }
                var articleForLangInDb = articleForLangService
                        .getArticleForLangByUrlRelativeAndLang(
                                articleDownloaded.urlRelative,
                                lang.id
                        )

                //set keys for article images.
                articleDownloaded.images.forEach {
                    it.articleForLangId = articleDownloaded.id
                }

                if (articleForLangInDb == null) {
                    articleForLangInDb = articleForLangService.insert(articleDownloaded.apply {
                        articleId = articleInDb.id
                        this.createdOnSite = createdOnSite
                        this.updatedOnSite = updatedOnSite
                    })
                } else {
                    articleForLangInDb = articleForLangService.insert(
                            articleForLangInDb.apply {
                                commentsUrl = articleDownloaded.commentsUrl
                                rating = articleDownloaded.rating
                                title = articleDownloaded.title
                                text = articleDownloaded.text
                                this.createdOnSite = createdOnSite
                                this.updatedOnSite = updatedOnSite
                            }
                    )
                }

                manageTagsForArticle(
                        lang.id,
                        articleDownloaded,
                        articleForLangInDb
                )

                //parse inner
                getAndSaveInnerArticles(lang, articleDownloaded, innerArticlesDepth)

                createArticleToArticleRelation(articleDownloaded, articleForLangInDb.id!!, lang)

                return articleForLangInDb
            } else {
                println("error in articles parsing: $urlRelative")
                return null
            }
        } catch (e: Exception) {
            println("error in articles parsing: $urlRelative $e")
            return null
        }
    }

    protected fun parseForRecentArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        val table = doc.getElementsByClass("wiki-content-table").first()
                ?: throw NullPointerException("Can't find element for class \"wiki-content-table\"")

        val articles = mutableListOf<ArticleForLang>()
        val listOfElements = table.getElementsByTag("tr")
        for (i in 1/*start from 1 as first row is tables header*/ until listOfElements.size) {
            val tableRow = listOfElements[i]
            val listOfTd = tableRow.getElementsByTag("td")
            //title and url
            val firstTd = listOfTd.first()
            val tagA = firstTd.getElementsByTag("a").first()
            val title = tagA.text()
            val url = lang.siteBaseUrl + tagA.attr("href")
            val rating = Integer.parseInt(listOfTd[1].text())
            //author
            val spanWithAuthor = listOfTd[2]
                    .getElementsByAttributeValueContaining("class", "printuser").first()
            //todo
            val authorName = spanWithAuthor.text()
            //todo
            val authorUrl = spanWithAuthor.getElementsByTag("a").first()?.attr("href")

            val createdDate = listOfTd[3].text().trim()
            val updatedDate = listOfTd[4].text().trim()

            val article = ArticleForLang(
                    langId = lang.id,
                    urlRelative = url.replace(lang.siteBaseUrl, "").trim(),
                    title = title,
                    rating = rating,
                    createdOnSite = Timestamp(getDateFormatForLang().parse(createdDate).time),
                    updatedOnSite = Timestamp(getDateFormatForLang().parse(updatedDate).time)
            )
            //todo
//            article.authorName = authorName
//            article.authorUrl = authorUrl
            articles.add(article)
        }

        return articles
    }

    fun parseArticleForLang(urlRelative: String, lang: Lang) {
        saveArticle(lang, urlRelative)
    }

    fun getArticleFromApi(url: String, lang: Lang): ArticleForLang? {
        val request = Request.Builder()
                .url(lang.siteBaseUrl + url)
                .build()

        var responseBody = okHttpClient.newCall(request).execute().body()?.string()
                ?: throw ScpParseException("error while getArticleFromApi")

        //remove all fucking RTL(&lrm) used for text-alignment. What a fucking idiots!..
        responseBody = responseBody.replace("[\\p{Cc}\\p{Cf}]".toRegex(), "")

        val doc = Jsoup.parse(responseBody)
        val pageContent = getArticlePageContentTag(doc)
                ?: throw ScpParseException("pageContent is NULL for: $url")
        val p404 = pageContent.getElementById("404-message")
        if (p404 != null) {
            return ArticleForLang(
                    langId = lang.id,
                    urlRelative = url,
                    title = "404",
                    text = p404.outerHtml()
            )
        }

        return parseHtmlService.parseArticle(url, doc, pageContent, lang)
    }

    /**
     * We need this as in FR site all article content wrapped in another div... ***!!!11
     *
     * @return Element with article content
     */
    protected fun getArticlePageContentTag(doc: Document): Element? =
            doc.getElementById(HTML_ID_PAGE_CONTENT)

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
            tagForLangService.getByLangIdAndTitleOrCreate(langId, it.title)
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

    fun getAndSaveInnerArticles(
            lang: Lang,
            articleDownloaded: ArticleForLang,
            maxDepth: Int = 0,
            currentDepthLevel: Int = 0
    ) {
        if (currentDepthLevel >= maxDepth) {
            return
        }
        println("getAndSaveInnerArticles: ${articleDownloaded.title}, $currentDepthLevel")

        val innerArticlesUrls = articleDownloaded.innerArticlesForLang.map { it.urlRelative }
        for (innerUrl in innerArticlesUrls) {
            println("save inner article: $innerUrl")
            try {
                val innerArticleDownloaded = saveArticle(lang, innerUrl) ?: continue

                getAndSaveInnerArticles(lang, innerArticleDownloaded, maxDepth, currentDepthLevel + 1)
            } catch (e: Exception) {
                println("error while save inner article: $e")
            } catch (e: ScpParseException) {
                println("error while save inner article: $e")
            }

        }
    }

    fun getObjectTypeByImageUrl(imageURL: String): ScpReaderConstants.ObjectType {
        val type: ScpReaderConstants.ObjectType

        when (imageURL) {
            "http://scp-ru.wdfiles.com/local--files/scp-list-4/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-3/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-2/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-ru/na(1).png",
            "http://scp-ru.wdfiles.com/local--files/scp-list/na.png",
            "http://scp-ru.wdfiles.com/local--files/archive/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-j/na(1).png",
                //other filials
            "http://scp-ru.wdfiles.com/local--files/scp-list-fr/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-jp/safe1.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-es/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-pl/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-de/safe1.png" ->
                type = ScpReaderConstants.ObjectType.NEUTRAL_OR_NOT_ADDED

            "http://scp-ru.wdfiles.com/local--files/scp-list-4/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-3/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-2/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-ru/safe(1).png",
            "http://scp-ru.wdfiles.com/local--files/scp-list/safe.png",
            "http://scp-ru.wdfiles.com/local--files/archive/safe.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-j/safe(1).png",
                //other filials
            "http://scp-ru.wdfiles.com/local--files/scp-list-fr/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-jp/na1.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-es/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-pl/na.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-de/na1.png" ->
                type = ScpReaderConstants.ObjectType.SAFE

            "http://scp-ru.wdfiles.com/local--files/scp-list-4/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-3/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-2/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-ru/euclid(1).png",
            "http://scp-ru.wdfiles.com/local--files/scp-list/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/archive/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-j/euclid(1).png",
                //other filials
            "http://scp-ru.wdfiles.com/local--files/scp-list-fr/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-jp/euclid1.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-es/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-pl/euclid.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-de/euclid1.png" ->
                type = ScpReaderConstants.ObjectType.EUCLID

            "http://scp-ru.wdfiles.com/local--files/scp-list-4/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-3/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-2/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-ru/keter(1).png",
            "http://scp-ru.wdfiles.com/local--files/scp-list/keter.png",
            "http://scp-ru.wdfiles.com/local--files/archive/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-j/keter(1).png",
                //other filials
            "http://scp-ru.wdfiles.com/local--files/scp-list-fr/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-jp/keter1.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-es/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-pl/keter.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-de/keter1.png" ->
                type = ScpReaderConstants.ObjectType.KETER

            "http://scp-ru.wdfiles.com/local--files/scp-list-4/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-3/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-2/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-ru/thaumiel(1).png",
            "http://scp-ru.wdfiles.com/local--files/scp-list/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/archive/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-j/thaumiel(1).png",
                //other filials
            "http://scp-ru.wdfiles.com/local--files/scp-list-fr/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-jp/thaumiel1.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-es/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-pl/thaumiel.png",
            "http://scp-ru.wdfiles.com/local--files/scp-list-de/thaumiel1.png" ->
                type = ScpReaderConstants.ObjectType.THAUMIEL

            else -> type = ScpReaderConstants.ObjectType.NONE
        }
        return type
    }

    companion object {
        const val HTML_ID_PAGE_CONTENT = "page-content"

        private const val DATE_FORMAT_PATTERN_EN = "dd MMM yyyy HH:mm"

        fun getDateFormatForLang() = SimpleDateFormat(DATE_FORMAT_PATTERN_EN, Locale.ENGLISH)

        const val DEFAULT_INNER_ARTICLES_DEPTH = 1
    }
}

class ScpParseException(
        override val message: String?,
        override val cause: Throwable? = null
) : Throwable(message, cause)
