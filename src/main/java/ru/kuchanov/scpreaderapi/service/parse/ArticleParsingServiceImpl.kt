package ru.kuchanov.scpreaderapi.service.parse

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.Article
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.configuration.NetworkConfiguration
import ru.kuchanov.scpreaderapi.repository.article.ArticlesForLangRepository
import ru.kuchanov.scpreaderapi.repository.article.ArticlesImagesRepository
import ru.kuchanov.scpreaderapi.repository.article.ArticlesRepository
import ru.kuchanov.scpreaderapi.service.article.ParseHtmlService
import java.io.IOException
import java.sql.Timestamp
import java.text.SimpleDateFormat


@Suppress("unused")
@Service
class ArticleParsingServiceImpl : ArticleParsingService {

    @Autowired
    private lateinit var logger: Logger

    @Autowired
    @Qualifier(NetworkConfiguration.QUALIFIER_OK_HTTP_CLIENT_NOT_LOGGING)
    private lateinit var okHttpClient: OkHttpClient

    @Autowired
    private lateinit var parseHtmlService: ParseHtmlService

    @Autowired
    private lateinit var articlesRepository: ArticlesRepository

    @Autowired
    private lateinit var articlesForLangRepository: ArticlesForLangRepository

    @Autowired
    private lateinit var articlesImagesRepository: ArticlesImagesRepository

    @Async
    override fun parseMostRecentArticlesForLang(lang: Lang, maxPageCount: Int?) {
        when (lang.id) {
            ScpReaderConstants.Firebase.FirebaseInstance.RU.lang -> {
                getRecentArticlesPageCountObservable()
                        .flatMapObservable { recentArticlesPagesCount ->
                            Observable.range(1, maxPageCount ?: recentArticlesPagesCount)
                        }
                        .flatMap { page ->
                            getRecentArticlesForPage(lang, page)
                                    .flatMapObservable { Observable.fromIterable(it) }
                        }
                        .toList()
                        .flatMap { downloadAndSaveArticles(it, lang) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribeBy(
                                onSuccess = {
                                    println("download complete")
                                    println(it.joinToString(separator = "\n\n"))
                                    println("download complete")
                                },
                                onError = {
                                    it.printStackTrace()
                                }
                        )
            }
            //todo
            else -> throw NotImplementedError()
        }
    }

    private fun getRecentArticlesForPage(lang: Lang, page: Int): Single<List<ArticleForLang>> {
        return Single.create<List<ArticleForLang>> { subscriber ->
            val request = Request.Builder()
                    //todo pass somehow
                    .url("http://scpfoundation.ru/most-recently-created" + "/p/" + page)
                    .build()

            val responseBody = okHttpClient
                    .newCall(request)
                    .execute()
                    .body()
                    ?.string()
                    ?: throw IOException("error while getRecentArticlesForPage: $page")
            val doc: Document = Jsoup.parse(responseBody)

            val articles = parseForRecentArticles(lang, doc)

            subscriber.onSuccess(articles)
        }
    }

    private fun getRecentArticlesPageCountObservable(): Single<Int> {
        return Single.create<Int> { subscriber ->
            val request = Request.Builder()
                    //todo pass somehow
                    .url("http://scpfoundation.ru/most-recently-created" + "/p/1")
                    .build()

            val responseBody: String
            try {
                val response = okHttpClient.newCall(request).execute()
                val body = response.body()
                if (body != null) {
                    responseBody = body.string()
                } else {
                    subscriber.onError(IOException("error while getRecentArticlesPageCount"))
                    return@create
                }
            } catch (e: IOException) {
                subscriber.onError(IOException("error while getRecentArticlesPageCount", e))
                return@create
            }

            val doc = Jsoup.parse(responseBody)

            //get num of pages
            val spanWithNumber = doc.getElementsByClass("pager-no").first()
            val text = spanWithNumber.text()
            val numOfPages = Integer.valueOf(text.substring(text.lastIndexOf(" ") + 1))

            subscriber.onSuccess(numOfPages)
        }
    }

    private fun downloadAndSaveArticles(articlesToDownload: List<ArticleForLang>, lang: Lang): Single<List<ArticleForLang>> {
        return Single.just(articlesToDownload)
                .flatMap { articles ->
                    for (articleToDownload in articles) {
                        try {
                            val articleDownloaded = getArticleFromApi(articleToDownload.urlRelative, lang)
                            if (articleDownloaded != null) {
                                var articleInDb = articlesRepository.getArticleByUrlRelative(articleDownloaded.urlRelative)
                                if (articleInDb == null) {
                                    articleInDb = articlesRepository.save(Article())
                                }
                                var articleForLangInDb = articlesForLangRepository
                                        .getArticleForLangByUrlRelativeAndLang(
                                                articleDownloaded.urlRelative,
                                                lang.id
                                        )

                                //set keys for article images.
                                articleDownloaded.images.forEach {
                                    it.articleUrlRelative = articleDownloaded.urlRelative
                                    it.articleLangId = articleDownloaded.langId
                                    it.articleId = articleInDb!!.id
                                }

                                if (articleForLangInDb == null) {
                                    articleForLangInDb = articlesForLangRepository.save(articleDownloaded.apply {
                                        articleId = articleInDb!!.id
                                        createdOnSite = articleToDownload.createdOnSite
                                        updatedOnSite = articleToDownload.updatedOnSite
                                    })
                                } else {
                                    articleForLangInDb = articlesForLangRepository.save(
                                            articleForLangInDb.apply {
                                                commentsUrl = articleDownloaded.commentsUrl
                                                rating = articleDownloaded.rating
                                                title = articleDownloaded.title
                                                text = articleDownloaded.text
                                                createdOnSite = articleToDownload.createdOnSite
                                                updatedOnSite = articleToDownload.updatedOnSite
                                                //todo add fields
                                            }
                                    )
                                }

                                //todo parse inner
//                                if (mMyPreferenceManager.isHasSubscription() && mInnerArticlesDepth !== 0) {
//                                    getAndSaveInnerArticles(dbProvider, getApiClient(), articleDownloaded, 0, mInnerArticlesDepth)
//                                }
                            } else {
                                println("error in articles parsing: ${articleToDownload.urlRelative}")
                                logger.warn("error in articles parsing: ${articleToDownload.urlRelative}")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    Single.just(articles)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    protected fun parseForRecentArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        val pageContent = doc.getElementsByClass("wiki-content-table").first()
                ?: throw NullPointerException("Can't find element for class \"wiki-content-table\"")

        val articles = mutableListOf<ArticleForLang>()
        val listOfElements = pageContent.getElementsByTag("tr")
        for (i in 1/*start from 1 as first row is tables header*/ until listOfElements.size) {
            val tableRow = listOfElements[i]
            val listOfTd = tableRow.getElementsByTag("td")
            //title and url
            val firstTd = listOfTd.first()
            val tagA = firstTd.getElementsByTag("a").first()
            val title = tagA.text()
            val url = lang.siteBaseUrl + tagA.attr("href")
            //rating
            val rating = Integer.parseInt(listOfTd[1].text())
            //author
            val spanWithAuthor = listOfTd[2]
                    .getElementsByAttributeValueContaining("class", "printuser").first()
            val authorName = spanWithAuthor.text()
            val authorUrl = spanWithAuthor.getElementsByTag("a").first()?.attr("href")

            val createdDate = listOfTd[3].text().trim()
            val updatedDate = listOfTd[4].text().trim()

            val article = ArticleForLang(
                    langId = lang.id,
                    urlRelative = url.replace(lang.siteBaseUrl, "").trim(),
                    title = title,
                    createdOnSite = Timestamp(DATE_FORMAT.parse(createdDate).time),
                    updatedOnSite = Timestamp(DATE_FORMAT.parse(updatedDate).time)
            )
            //todo
//            article.rating = rating
//            article.authorName = authorName
//            article.authorUrl = authorUrl
            articles.add(article)
        }

        return articles
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
    protected fun getArticlePageContentTag(doc: Document): Element? {
        return doc.getElementById(HTML_ID_PAGE_CONTENT)
    }

    companion object {
        const val HTML_ID_PAGE_CONTENT = "page-content"
        /**
         * i.e. 05:33 02.12.2018`
         */
//        private val DATE_FORMAT = SimpleDateFormat("HH:mm dd.MM.yyyy")
        private val DATE_FORMAT = SimpleDateFormat("dd MMM yyyy HH:mm")
    }
}

class ScpParseException(
        override val message: String?,
        override val cause: Throwable? = null
) : Throwable(message, cause)