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
class ArticleParsingServiceImplIT : ArticleParsingServiceBase() {

    override fun parseMostRecentArticlesForLang(lang: Lang, maxPageCount: Int?, processOnlyCount: Int?) {
        super.parseMostRecentArticlesForLang(lang, maxPageCount, processOnlyCount)
    }

    override fun getMostRecentArticlesPageCountForLang(lang: Lang): Single<Int> {
        return Single.create<Int> { subscriber: SingleEmitter<Int> ->
            val request: Request = Request.Builder()
                    .url(lang.siteBaseUrl + ScpReaderConstants.RecentArticlesUrl.IT)
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
                    .url(lang.siteBaseUrl + ScpReaderConstants.RecentArticlesUrl.IT + page)
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
        //TODO
        /*
        java.lang.NullPointerException
	at ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceImplIT.parseForRecentArticles(ArticleParsingServiceImplIT.kt:83)
	at ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceImplIT$getRecentArticlesForPage$1.subscribe(ArticleParsingServiceImplIT.kt:75)
	at io.reactivex.internal.operators.single.SingleCreate.subscribeActual(SingleCreate.java:39)
	at io.reactivex.Single.subscribe(Single.java:3433)
	at io.reactivex.internal.operators.mixed.SingleFlatMapObservable.subscribeActual(SingleFlatMapObservable.java:49)
	at io.reactivex.Observable.subscribe(Observable.java:12084)
	at io.reactivex.internal.operators.observable.ObservableFlatMap$MergeObserver.subscribeInner(ObservableFlatMap.java:165)
	at io.reactivex.internal.operators.observable.ObservableFlatMap$MergeObserver.onNext(ObservableFlatMap.java:139)
	at io.reactivex.internal.operators.mixed.SingleFlatMapObservable$FlatMapObserver.onNext(SingleFlatMapObservable.java:69)
	at io.reactivex.internal.operators.observable.ObservableRange$RangeDisposable.run(ObservableRange.java:64)
	at io.reactivex.internal.operators.observable.ObservableRange.subscribeActual(ObservableRange.java:35)
	at io.reactivex.Observable.subscribe(Observable.java:12084)
	at io.reactivex.internal.operators.mixed.SingleFlatMapObservable$FlatMapObserver.onSuccess(SingleFlatMapObservable.java:109)
	at io.reactivex.internal.operators.single.SingleCreate$Emitter.onSuccess(SingleCreate.java:67)
	at ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceImplIT$getMostRecentArticlesPageCountForLang$1.subscribe(ArticleParsingServiceImplIT.kt:52)
	at io.reactivex.internal.operators.single.SingleCreate.subscribeActual(SingleCreate.java:39)
	at io.reactivex.Single.subscribe(Single.java:3433)
	at io.reactivex.internal.operators.mixed.SingleFlatMapObservable.subscribeActual(SingleFlatMapObservable.java:49)
	at io.reactivex.Observable.subscribe(Observable.java:12084)
	at io.reactivex.internal.operators.observable.ObservableFlatMap.subscribeActual(ObservableFlatMap.java:55)
	at io.reactivex.Observable.subscribe(Observable.java:12084)
	at io.reactivex.internal.operators.observable.ObservableToListSingle.subscribeActual(ObservableToListSingle.java:58)
	at io.reactivex.Single.subscribe(Single.java:3433)
	at io.reactivex.internal.operators.single.SingleMap.subscribeActual(SingleMap.java:34)
	at io.reactivex.Single.subscribe(Single.java:3433)
	at io.reactivex.internal.operators.single.SingleFlatMap.subscribeActual(SingleFlatMap.java:36)
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
        val contentTypeDescription = doc.getElementsByClass("content-type-description").first()
        val pageContent = contentTypeDescription.getElementsByTag("table").first()
                ?: throw ScpParseException("parse error!")

        val articles: MutableList<ArticleForLang> = ArrayList()
        val listOfElements: Elements = pageContent.getElementsByTag("tr")
        for (i in 1 /*start from 1 as first row is tables header*/ until listOfElements.size) {
            val listOfTd: Elements = listOfElements[i].getElementsByTag("td")
            val firstTd: Element = listOfTd.first()
            val tagA = firstTd.getElementsByTag("a").first()
            val title = tagA.text()
            val url: String = lang.siteBaseUrl + tagA.attr("href")
            //4 Jun 2017, 22:25
//createdDate
            val createdDateNode: Element = listOfTd[1]
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
        return super.getArticleFromApi(url, lang)
    }

    override fun getArticlePageContentTag(doc: Document): Element? {
        return super.getArticlePageContentTag(doc)
    }

    override fun getAndSaveInnerArticles(lang: Lang, articleDownloaded: ArticleForLang, maxDepth: Int, currentDepthLevel: Int) {
        super.getAndSaveInnerArticles(lang, articleDownloaded, maxDepth, currentDepthLevel)
    }
}