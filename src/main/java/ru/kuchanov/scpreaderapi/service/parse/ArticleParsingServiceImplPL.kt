package ru.kuchanov.scpreaderapi.service.parse

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import java.sql.Timestamp
import java.util.*


@Service
class ArticleParsingServiceImplPL : ArticleParsingServiceBase() {

    override fun getRatedArticlesUrl() = "/najwyzej-ocenione/p/"

    override fun getRecentArticlesUrl() = "/ostatnio-stworzone/p/"

    override fun getObjectArticlesUrls(): List<String> {
        return listOf(
                "/lista-eng",
                "/lista-eng-2",
                "/lista-eng-3",
                "/lista-eng-4",
                "/lista-eng-5"
        )
    }

    override fun parseForRecentArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        val contentTypeDescription = doc.getElementsByClass("content-type-description").first()
        val pageContent = contentTypeDescription.getElementsByTag("table").first()
                ?: throw ScpParseException("parse error!")

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
        val pageContent = doc.getElementById("page-content")
                ?: throw ScpParseException("parse error!")
        val listPagesBox = pageContent.getElementsByClass("list-pages-box").first()
                ?: throw ScpParseException("parse error!")
        val articlesDivs = listPagesBox.getElementsByClass("list-pages-item")
        val articles: MutableList<ArticleForLang> = ArrayList()
        for (element in articlesDivs) {
            val aTag = element.getElementsByTag("a").first()
            val url: String = lang.siteBaseUrl + aTag.attr("href")
            val title = aTag.text()
            val pTag = element.getElementsByTag("p").first()
            var ratingString = pTag.text().substring(pTag.text().indexOf("Ocena: ") + "Ocena: ".length)
            println("ratingString: $ratingString")
            ratingString = ratingString.substring(0, ratingString.indexOf(", Komentarze"))
            println("ratingString: $ratingString")
            val rating = ratingString.toInt()
            //TODO parse date
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
}