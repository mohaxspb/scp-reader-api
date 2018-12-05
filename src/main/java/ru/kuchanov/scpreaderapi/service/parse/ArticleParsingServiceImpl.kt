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
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.service.article.ParseHtmlService
import java.io.IOException


@Service
class ArticleParsingServiceImpl : ArticleParsingService {

    @Autowired
    private lateinit var okHttpClient: OkHttpClient

    @Autowired
    private lateinit var parseHtmlService: ParseHtmlService

    @Async
    override fun parseMostRecentArticlesForLang(lang: Lang, maxPageCount: Int?) {
//        val articles = mutableListOf<Article>()

        when (lang.id) {
            ScpReaderConstants.Firebase.FirebaseInstance.RU.lang -> {
                val subscription = getRecentArticlesPageCountObservable()
                        .flatMapObservable { Observable.range(1, maxPageCount ?: it) }
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
                                    println(it)
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
            val doc = Jsoup.parse(responseBody)

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
                                //todo save article, relative etc
//                                saveArticle(articleDownloaded)

                                //todo parse inner
//                                if (mMyPreferenceManager.isHasSubscription() && mInnerArticlesDepth !== 0) {
//                                    getAndSaveInnerArticles(dbProvider, getApiClient(), articleDownloaded, 0, mInnerArticlesDepth)
//                                }
                            } else {
                                //todo log error in articles parsing
                            }
                        } catch (e: Exception) {
                            println(e)
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

            val article = ArticleForLang(
                    langId = lang.id,
                    urlRelative = url.trim(),
                    title = title
            )
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

    fun getArticleFromApi(url: String, lang: Lang): ArticleForLang? {
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
    }
}

class ScpParseException(
        override val message: String?,
        override val cause: Throwable? = null
) : Throwable(message, cause)