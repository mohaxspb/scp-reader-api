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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.Article
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.service.article.ParseHtmlService
import java.io.IOException
import java.util.*


@Service
class ArticleParsingServiceImpl : ArticleParsingService {

    @Autowired
    private lateinit var okHttpClient: OkHttpClient

    @Autowired
    private lateinit var parseHtmlService:ParseHtmlService

    @Async
    override fun parseMostRecentArticlesForLang(lang: Lang) {
        val articles = mutableListOf<Article>()

        when (lang.id) {
            ScpReaderConstants.Firebase.FirebaseInstance.RU.lang -> {
                val subscription = getRecentArticlesPageCountObservable()
                        .flatMapObservable { Observable.range(1, it) }
                        .flatMap { page ->
                            getRecentArticlesForPage(lang, page)
                                    .flatMapObservable { Observable.fromIterable(it) }
                        }
                        .toList()
                        .flatMap { downloadAndSaveArticles(it, lang) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribeBy(
                                {
                                    println("download complete")
                                }
                        )
            }
            //todo
            else -> throw NotImplementedError()
        }
    }

    private fun getRecentArticlesForPage(lang: Lang, page: Int): Single<List<Article>> {
        return Single.create<List<Article>> { subscriber ->
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
            try {
                val doc = Jsoup.parse(responseBody)

                val articles = parseForRecentArticles(lang, doc)

                subscriber.onSuccess(articles)
            } catch (e: Exception) {
                subscriber.onError(e)
            }
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

            try {
                val doc = Jsoup.parse(responseBody)

                //get num of pages
                val spanWithNumber = doc.getElementsByClass("pager-no").first()
                val text = spanWithNumber.text()
                val numOfPages = Integer.valueOf(text.substring(text.lastIndexOf(" ") + 1))

                subscriber.onSuccess(numOfPages)
            } catch (e: Exception) {
                e.printStackTrace()
                subscriber.onError(e)
            }
        }
    }

    private fun downloadAndSaveArticles(articlesToDownload: List<Article>, lang: Lang): Single<List<Article>> {
        return Single.just(articlesToDownload)
                .flatMap { articles ->
                    for (articleToDownload in articles) {
                        try {
                            val articleDownloaded = getArticleFromApi(articleToDownload.getUrl(), lang)
                            if (articleDownloaded != null) {
                                saveArticle(articleDownloaded)

                                //todo
//                                if (mMyPreferenceManager.isHasSubscription() && mInnerArticlesDepth !== 0) {
//                                    getAndSaveInnerArticles(dbProvider, getApiClient(), articleDownloaded, 0, mInnerArticlesDepth)
//                                }
                            } else {
                                //todo log error in articles parsing
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

    protected fun parseForRecentArticles(lang: Lang, doc: Document): List<Article> {
        val pageContent = doc.getElementsByClass("wiki-content-table").first()
                ?: throw NullPointerException("Can't find element for class \"wiki-content-table\"")

        val articles = ArrayList<Article>()
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
            val ratingNode = listOfTd[1]
            val rating = Integer.parseInt(ratingNode.text())
            //author
            val spanWithAuthor = listOfTd[2]
                    .getElementsByAttributeValueContaining("class", "printuser").first()
            val authorName = spanWithAuthor.text()
            val authorUrl = spanWithAuthor.getElementsByTag("a").first()?.attr("href")

            //createdDate
            val createdDateNode = listOfTd[3]
            val createdDate = createdDateNode.text().trim()
            //updatedDate
            val updatedDateNode = listOfTd[4]
            val updatedDate = updatedDateNode.text().trim()

            val article = Article()
            //todo
//            article.title = title
//            article.url = url.trim { it <= ' ' }
//            article.rating = rating
//            article.authorName = authorName
//            article.authorUrl = authorUrl
//            article.createdDate = createdDate
//            article.updatedDate = updatedDate
            articles.add(article)
        }

        return articles
    }

    fun getArticleFromApi(url: String, lang: Lang): Article? {
        val request = Request.Builder()
                .url(url)
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
            val article = Article()
            //todo
//            article.url = url
//            article.text = p404!!.outerHtml()
//            article.title = "404"

            return article
        }

        try {
            return parseHtmlService.parseArticle(url, doc, pageContent, lang)
        } catch (e: Exception) {
            throw e
        }
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
    }
}

class ScpParseException(
        override val message: String?,
        override val cause: Throwable? = null
) : Throwable(message, cause)