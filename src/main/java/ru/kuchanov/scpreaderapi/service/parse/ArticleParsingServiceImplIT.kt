package ru.kuchanov.scpreaderapi.service.parse

import io.reactivex.Single
import io.reactivex.SingleEmitter
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.Article
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import java.io.IOException
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
                println("Num of pages Italy: $numOfPages")
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
        return listOf()
    }


    override fun parseForRatedArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        var doc = doc
        val pageContent = doc.getElementById("page-content")
                ?: throw ScpParseException("parse error!")
        val listPagesBox = pageContent.getElementsByClass("list-pages-box").first()
                ?: throw ScpParseException("parse error!")
        val allArticles = listPagesBox.getElementsByTag("p").first().html()
        val arrayOfArticles = allArticles.split("<br>").toTypedArray()
        val articles: MutableList<ArticleForLang> = ArrayList()
        for (arrayItem in arrayOfArticles) {
            doc = Jsoup.parse(arrayItem)
            val aTag = doc.getElementsByTag("a").first()
            val url: String = lang.siteBaseUrl + aTag.attr("href")
            val title = aTag.text()
            var rating = arrayItem.substring(arrayItem.indexOf("Voto: ") + "Voto: ".length)
            rating = rating.substring(0, rating.indexOf(", "))
            val article = ArticleForLang(
                    langId = lang.id,
                    urlRelative = url.replace(lang.siteBaseUrl, "").trim(),
                    title = title,
                    rating = rating.toInt()
            )
            articles.add(article)
        }
        return articles
    }

    override fun getRatedArticlesForLang(lang: Lang, page: Int): Single<List<ArticleForLang>> {
        return Single.create { subscriber: SingleEmitter<List<ArticleForLang>> ->

            val request: Request = Request.Builder()
                    .url(lang.siteBaseUrl + ScpReaderConstants.RatedArticlesUrl.IT + page)
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
}