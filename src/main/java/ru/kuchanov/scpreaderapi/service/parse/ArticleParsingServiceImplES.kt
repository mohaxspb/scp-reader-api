package ru.kuchanov.scpreaderapi.service.parse

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import java.sql.Timestamp
import java.util.*


@Service
class ArticleParsingServiceImplES : ArticleParsingServiceBase() {

    override fun getRatedArticlesUrl() = "/paginas-mejor-puntuadas/p/"

    override fun getRecentArticlesUrl() = "/recientemente-creados/p/"

    override fun getObjectArticlesUrls(): List<String> {
        return listOf(
                "/serie-scp-1",
                "/serie-scp-2",
                "/serie-scp-3",
                "/serie-scp-4",
                "/serie-scp-5"
        )
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
            var rating = arrayItem.substring(arrayItem.indexOf(" (+") + " (+".length)
            rating = rating.substring(0, rating.indexOf(")"))
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

    override fun getArticleFromApi(url: String, lang: Lang): ArticleForLang? {

        //TODO check commentsUrl for ES site
        /*
        18:31:24.054 error in articles parsing: /hub-del-sarkicismo
java.lang.NullPointerException: null
         */
        return super.getArticleFromApi(url, lang)
    }

}