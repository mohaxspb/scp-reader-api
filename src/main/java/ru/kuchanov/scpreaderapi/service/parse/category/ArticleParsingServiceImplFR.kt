package ru.kuchanov.scpreaderapi.service.parse.category

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants
import java.sql.Timestamp


@Service
class ArticleParsingServiceImplFR : ArticleParsingServiceBase() {

    override fun getRatedArticlesUrl() = "/top-rated-pages/p/"

    override fun getRecentArticlesUrl() = "/most-recently-created/p/"

    override fun getObjectArticlesUrls(): List<String> {
        return listOf(
            "/scp-series",
            "/scp-series-2",
            "/scp-series-3",
            "/scp-series-4",
            "/scp-series-5"
        )
    }

    override fun parseForRecentArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        val contentTypeDescription = doc.getElementsByClass("content-type-description").first()
        val recentWithDatesTag = contentTypeDescription.getElementsByClass("collapsible-block").first()
        val recentWithDatesSpoilerTitleTag = recentWithDatesTag.getElementsByClass("collapsible-block-link").first()
        val recentWithDatesSpoilerTitle = recentWithDatesSpoilerTitleTag.text().replace("\\p{Z}".toRegex(), " ")
        if (recentWithDatesSpoilerTitle != "+ Avec date") {
            throw IllegalStateException("Can't find recent articles table with date!")
        }
        val pageContent = recentWithDatesTag.getElementsByTag(ParseConstants.TAG_TABLE).first()
            ?: throw ScpParseException("parse error!")

        val dateFormat = getDateFormatForLang()
        val articles = mutableListOf<ArticleForLang>()
        val listOfElements = pageContent.getElementsByTag("tr")
        for (i in 1 /*start from 1 as first row is tables header*/ until listOfElements.size) {
            val listOfTd: Elements = listOfElements[i].getElementsByTag("td")
            val firstTd: Element = listOfTd.first()
            val tagA = firstTd.getElementsByTag(ParseConstants.TAG_A).first()
            val title = tagA.text()
            val url = tagA.attr(ParseConstants.ATTR_HREF)
            //4 Jun 2017, 22:25
            //createdDate
            val createdDateNode: Element = listOfTd[1]
            val createdDate = createdDateNode.text().trim()
            val article = ArticleForLang(
                langId = lang.id,
                urlRelative = lang.removeDomainFromUrl(url),
                title = title,
                createdOnSite = Timestamp(dateFormat.parse(createdDate).time)
            )
            articles.add(article)
        }

        return articles
    }

    override fun parseForRatedArticles(lang: Lang, doc: Document) =
        parseForRatedArticlesENStyle(
            lang,
            doc,
            getArticleRatingStringDelimiter(),
            getArticleRatingStringDelimiterEnd(),
            1,
            logger = log
        )

    override fun parseForObjectArticles(lang: Lang, doc: Document) =
        parseForObjectArticlesENStyle(lang, doc, logger = log)

    override fun getArticleRatingStringDelimiter() = " ("

    override fun getArticleRatingStringDelimiterEnd() = ")"
}
