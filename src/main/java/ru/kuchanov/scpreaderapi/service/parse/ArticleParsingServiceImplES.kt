package ru.kuchanov.scpreaderapi.service.parse

import io.reactivex.Single
import io.reactivex.SingleEmitter
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import java.io.IOException
import java.sql.Timestamp
import java.util.*


@Service
class ArticleParsingServiceImplES : ArticleParsingServiceBase() {

    override fun getParsingRealizationForLang(lang: Lang): ArticleParsingServiceBase {
        return super.getParsingRealizationForLang(lang)
    }

    override fun parseMostRecentArticlesForLang(lang: Lang, maxPageCount: Int?, processOnlyCount: Int?) {
        super.parseMostRecentArticlesForLang(lang, maxPageCount, processOnlyCount)
    }

    override fun getMostRecentArticlesPageCountForLang(lang: Lang): Single<Int> {
        return Single.create<Int> { subscriber: SingleEmitter<Int> ->
            val request: Request = Request.Builder()
                    .url(lang.siteBaseUrl + ScpReaderConstants.RecentArticlesUrl.ES)
                    .build()
            val responseBody: String
            responseBody = try {
                val response: Response = okHttpClient.newCall(request).execute()
                val body = response.body()
                if (body != null) {
                    body.string()
                } else {
                    subscriber.onError(ScpParseException("parse error!!!"))
                    return@create
                }
            } catch (e: IOException) {
                subscriber.onError(IOException("connection error!"))
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

    override fun getRecentArticlesForPage(lang: Lang, page: Int): Single<List<ArticleForLang>> {
        return Single.create<List<ArticleForLang>> {

            val request = Request.Builder()
                    .url(lang.siteBaseUrl + ScpReaderConstants.RecentArticlesUrl.ES + page)
                    .build()

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

    override fun parseForRecentArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        val contentTypeDescription = doc.getElementsByClass("content-type-description").first()
        val pageContent = contentTypeDescription.getElementsByTag("table").first()
                ?: throw ScpParseException("parse error!!")

        val articles: MutableList<ArticleForLang> = ArrayList()
        val listOfElements: Elements = pageContent.getElementsByTag("tr")
        for (i in 1 /*start from 1 as first row is tables header*/ until listOfElements.size) {
            val listOfTd: Elements = listOfElements.get(i).getElementsByTag("td")
            val firstTd: Element = listOfTd.first()
            val tagA = firstTd.getElementsByTag("a").first()
            val title = tagA.text()
            val url: String = lang.siteBaseUrl + tagA.attr("href")
            //4 Jun 2017, 22:25
//createdDate
            val createdDateNode: Element = listOfTd.get(1)
            val createdDate = createdDateNode.text().trim()
            val article = ArticleForLang(
                    langId = lang.id,
                    urlRelative = url.replace(lang.siteBaseUrl, "").trim(),
                    title = title,
                    createdOnSite = Timestamp(DATE_FORMAT.parse(createdDate).time)
            )
            articles.add(article)
        }

        return articles
    }

    override fun parseArticleForLang(urlRelative: String, lang: Lang) {
        super.parseArticleForLang(urlRelative, lang)
    }

    override fun getArticleFromApi(url: String, lang: Lang): ArticleForLang? {

        //TODO
        /*
        18:31:24.054 error in articles parsing: /hub-del-sarkicismo
java.lang.NullPointerException: null
	at ru.kuchanov.scpreaderapi.service.article.ParseHtmlService.parseArticle(ParseHtmlService.kt:269)
	at ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceBase.getArticleFromApi(ArticleParsingServiceBase.kt:340)
	at ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceImplES.getArticleFromApi(ArticleParsingServiceImplES.kt:119)
	at ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceBase.saveArticle(ArticleParsingServiceBase.kt:207)
	at ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceBase.saveArticle$default(ArticleParsingServiceBase.kt:204)
	at ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceBase.getAndSaveInnerArticles(ArticleParsingServiceBase.kt:416)
	at ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceImplES.getAndSaveInnerArticles(ArticleParsingServiceImplES.kt:127)
	at ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceBase.getAndSaveInnerArticles$default(ArticleParsingServiceBase.kt:405)
	at ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceBase.saveArticle(ArticleParsingServiceBase.kt:251)
	at ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceBase$downloadAndSaveArticles$1.apply(ArticleParsingServiceBase.kt:186)
	at ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceBase$downloadAndSaveArticles$1.apply(ArticleParsingServiceBase.kt:35)
	at io.reactivex.internal.operators.single.SingleMap$MapSingleObserver.onSuccess(SingleMap.java:57)
	at io.reactivex.internal.operators.single.SingleJust.subscribeActual(SingleJust.java:30)
	at io.reactivex.Single.subscribe(Single.java:3433)
	at io.reactivex.internal.operators.single.SingleMap.subscribeActual(SingleMap.java:34)
	at io.reactivex.Single.subscribe(Single.java:3433)
	at io.reactivex.internal.operators.single.SingleSubscribeOn$SubscribeOnObserver.run(SingleSubscribeOn.java:89)
	at io.reactivex.Scheduler$DisposeTask.run(Scheduler.java:579)
	at io.reactivex.internal.schedulers.ScheduledRunnable.run(ScheduledRunnable.java:66)
	at io.reactivex.internal.schedulers.ScheduledRunnable.call(ScheduledRunnable.java:57)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$201(ScheduledThreadPoolExecutor.java:180)
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:293)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
         */
        return super.getArticleFromApi(url, lang)
    }

    override fun getArticlePageContentTag(doc: Document): Element? {
        return super.getArticlePageContentTag(doc)
    }

    override fun getAndSaveInnerArticles(lang: Lang, articleDownloaded: ArticleForLang, maxDepth: Int, currentDepthLevel: Int) {
        super.getAndSaveInnerArticles(lang, articleDownloaded, maxDepth, currentDepthLevel)
    }
}