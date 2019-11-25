package ru.kuchanov.scpreaderapi.service.parse

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import java.sql.Timestamp


@Suppress("unused")
@Service
class ArticleParsingServiceImplEN : ArticleParsingServiceBase() {

    override fun getRatedArticlesUrl() = "/top-rated-pages/p/"

    override fun getRecentArticlesUrl() = "/most-recently-created/p/"

    override fun getObjectArticlesUrls() =
            listOf(
                    "/scp-series",
                    "/scp-series-2",
                    "/scp-series-3",
                    "/scp-series-4",
                    "/scp-series-5"
            )

    override fun parseForRecentArticles(lang: Lang, doc: Document) =
            parseForRecentArticlesENStyle(lang, doc)

    override fun parseForRatedArticles(lang: Lang, doc: Document) =
            parseForRatedArticlesENStyle(lang, doc, getArticleRatingStringDelimiter(), getArticleRatingStringDelimiterEnd())

    override fun getArticleRatingStringDelimiter() = "rating: "

    override fun getArticleRatingStringDelimiterEnd() = ", "
}

fun parseForRatedArticlesENStyle(
        lang: Lang,
        doc: Document,
        articleRatingStringDelimiter: String,
        articleRatingStringDelimiterEnd: String
): List<ArticleForLang> {
    val pageContent = doc.getElementById("page-content")
            ?: throw ScpParseException("parse error!")
    val listPagesBox = pageContent.getElementsByClass("list-pages-box").first()
            ?: throw ScpParseException("parse error!")
    val allArticles = listPagesBox.getElementsByTag("p").first().html()
    val arrayOfArticles = allArticles.split("<br>").toTypedArray()
    val articles = mutableListOf<ArticleForLang>()
    for (arrayItem in arrayOfArticles) {
        val currentDocument = Jsoup.parse(arrayItem)
        val aTag = currentDocument.getElementsByTag("a").first()
        val url: String = lang.siteBaseUrl + aTag.attr("href")
        val title = aTag.text()
        var rating = arrayItem.substring(arrayItem.indexOf(articleRatingStringDelimiter) + articleRatingStringDelimiter.length)
        rating = rating.substring(0, rating.indexOf(articleRatingStringDelimiterEnd))
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

fun parseForRecentArticlesENStyle(lang: Lang, doc: Document): List<ArticleForLang> {
    val contentTypeDescription = doc.getElementsByClass("content-type-description").first()
    val pageContent = contentTypeDescription.getElementsByTag("table").first()
            ?: throw ScpParseException("parse error!")

    val articles = mutableListOf<ArticleForLang>()
    val listOfElements = pageContent.getElementsByTag("tr")
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
                createdOnSite = Timestamp(ArticleParsingServiceBase.DATE_FORMAT.parse(createdDate).time)
        )
        articles.add(article)
    }

    return articles
}