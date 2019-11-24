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
import ru.kuchanov.scpreaderapi.bean.articles.Article
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import java.io.IOException
import java.sql.Timestamp
import java.util.*


@Service
class ArticleParsingServiceImplPT : ArticleParsingServiceBase() {

    override fun parseMostRecentArticlesForLang(lang: Lang, maxPageCount: Int?, processOnlyCount: Int?) {
        super.parseMostRecentArticlesForLang(lang, maxPageCount, processOnlyCount)
    }

    override fun getMostRecentArticlesPageCountForLang(lang: Lang): Single<Int> {

        return Single.create<Int> { subscriber: SingleEmitter<Int> ->
            val request: Request = Request.Builder()
                    .url(lang.siteBaseUrl + ScpReaderConstants.RecentArticlesUrl.PT)
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
                    .url(lang.siteBaseUrl + ScpReaderConstants.RecentArticlesUrl.PT + page)
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
                ?: throw ScpParseException("parse error !!!")

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

    override fun parseForRatedArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        val pageContent = doc.getElementById("page-content")
                ?: throw ScpParseException("parse error!")
        val listPagesBox = pageContent.getElementsByClass("panel-body").last()
                ?: throw ScpParseException("parse error!")
        val articlesDivs = listPagesBox.getElementsByClass("list-pages-item")
        val articles: MutableList<ArticleForLang> = ArrayList()
        for (element in articlesDivs) {
            val aTag = element.getElementsByTag("a").first()
            val url: String = lang.siteBaseUrl + aTag.attr("href")
            val title = aTag.text()
            val pTag = element.getElementsByTag("p").first()
            var ratingString = pTag.text().substring(pTag.text().indexOf("avaliação ") + "avaliação ".length)
            ratingString = ratingString.substring(0, ratingString.indexOf("."))
            val rating = ratingString.toInt()
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

    override fun getRatedArticlesForLang(lang: Lang, page: Int): Single<List<ArticleForLang>> {
        return Single.create { subscriber: SingleEmitter<List<ArticleForLang>> ->

            val request: Request = Request.Builder()
                    .url(lang.siteBaseUrl + ScpReaderConstants.RatedArticlesUrl.PT + page)
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

    override fun getArticleFromApi(url: String, lang: Lang): ArticleForLang? {
        //TODO check commentsUrl parsing (http://scp-pt-br.wikidot.comjavascript:;)
        
        return super.getArticleFromApi(url, lang)
    }


}