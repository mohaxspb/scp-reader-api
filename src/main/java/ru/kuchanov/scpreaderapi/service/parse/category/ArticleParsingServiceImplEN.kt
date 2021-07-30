package ru.kuchanov.scpreaderapi.service.parse.category

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.slf4j.Logger
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ATTR_HREF
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ID_PAGE_CONTENT
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_A
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_P
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_TABLE
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_UL
import java.sql.Timestamp


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
            "/scp-series-5",
            "/scp-series-6"
        )

    override fun parseForRecentArticles(lang: Lang, doc: Document) =
        parseForRecentArticlesENStyle(lang, doc)

    override fun parseForRatedArticles(lang: Lang, doc: Document) =
        parseForRatedArticlesENStyle(
            lang,
            doc,
            getArticleRatingStringDelimiter(),
            getArticleRatingStringDelimiterEnd(),
            logger = log
        )

    override fun parseForObjectArticles(lang: Lang, doc: Document) =
        parseForObjectArticlesENStyle(lang, doc, log)

    override fun getArticleRatingStringDelimiter() = "rating: "

    override fun getArticleRatingStringDelimiterEnd() = ", "
}

fun parseForRatedArticlesENStyle(
    lang: Lang,
    doc: Document,
    articleRatingStringDelimiter: String,
    articleRatingStringDelimiterEnd: String,
    articlesListContainerNumber: Int = 0,
    logger: Logger
): List<ArticleForLang> {
    logger.debug("start parsing rated articles for lang: $lang")
    val pageContent = doc.getElementById(ID_PAGE_CONTENT)
        ?: throw ScpParseException("$ID_PAGE_CONTENT is null!", NullPointerException())
    val listPagesBox = pageContent.getElementsByClass("list-pages-box")[articlesListContainerNumber]
        ?: throw ScpParseException("list-pages-box is null!", NullPointerException())
    val allArticles = listPagesBox.getElementsByTag(TAG_P).first().html()
    val arrayOfArticles = allArticles.split("<br>").toTypedArray()
    logger.debug("arrayOfArticles: ${arrayOfArticles.size}")
    val articles = mutableListOf<ArticleForLang>()
    for (arrayItem in arrayOfArticles) {
        val currentDocument = Jsoup.parse(arrayItem)
        val aTag = currentDocument.getElementsByTag(TAG_A).first()
        val url = aTag.attr(ATTR_HREF)
        val title = aTag.text()
        val rating =
            arrayItem.substring(arrayItem.indexOf(articleRatingStringDelimiter) + articleRatingStringDelimiter.length)
        //logger.debug("rating: $rating")
        //logger.debug("articleRatingStringDelimiterEnd: $articleRatingStringDelimiterEnd")
        //logger.debug("rating.indexOf(articleRatingStringDelimiterEnd): ${rating.indexOf(articleRatingStringDelimiterEnd)}")
        val ratingCuted = rating.substring(0, rating.indexOf(articleRatingStringDelimiterEnd))
        val article = ArticleForLang(
            langId = lang.id,
            urlRelative = lang.removeDomainFromUrl(url),
            title = title,
            rating = try {
                ratingCuted.toInt()
            } catch (e: Exception) {
                logger.debug("=========================================")
                logger.debug("arrayItem: $arrayItem")
                logger.debug("rating: $rating")
                logger.debug("articleRatingStringDelimiter: $articleRatingStringDelimiter")
                logger.debug("articleRatingStringDelimiterEnd: $articleRatingStringDelimiterEnd")
                logger.debug("ratingCuted: $ratingCuted")
                logger.debug("=========================================")
                0
            }
        )
        articles.add(article)
    }
    return articles
}

fun parseForRecentArticlesENStyle(lang: Lang, doc: Document): List<ArticleForLang> {
    val contentTypeDescription = doc.getElementsByClass("content-type-description").first()
    val pageContent = contentTypeDescription.getElementsByTag(TAG_TABLE).first()
        ?: throw ScpParseException("parse error!")

    val dateFormat = ArticleParsingServiceBase.getDateFormatForLang()
    val articles = mutableListOf<ArticleForLang>()
    val listOfElements = pageContent.getElementsByTag("tr")
    for (i in 1 /*start from 1 as first row is tables header*/ until listOfElements.size) {
        val listOfTd: Elements = listOfElements[i].getElementsByTag("td")
        val firstTd: Element = listOfTd.first()
        val tagA = firstTd.getElementsByTag(TAG_A).first()
        val title = tagA.text()
        val url = tagA.attr(ATTR_HREF)
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

fun parseForObjectArticlesENStyle(lang: Lang, doc: Document, logger: Logger): List<ArticleForLang> {
    val pageContent = doc.getElementById(ID_PAGE_CONTENT)
        ?: throw ScpParseException("Parse error! \"page-content\" tag is null!")
    val listPagesBox = pageContent.getElementsByTag("h1")
    listPagesBox.remove()
    val collapsibleBlock: Element? = pageContent.getElementsByTag(TAG_UL).first()
    collapsibleBlock?.remove()
    val table: Element? = pageContent.getElementsByClass("content-toc").first()
    table?.remove()
    val allUls = pageContent.getElementsByClass("content-panel").first()?.getElementsByTag(TAG_UL) ?: listOf<Element>()

    val articles = mutableListOf<ArticleForLang>()

    for (ul in allUls) {
        for (li in ul.children()) { //do not add empty articles
            if (li.getElementsByTag(TAG_A).first().hasClass("newpage")) {
                continue
            }
            val title = li.text()
            val url = li.getElementsByTag(TAG_A).first().attr(ATTR_HREF)
            val article = ArticleForLang(
                langId = lang.id,
                urlRelative = lang.removeDomainFromUrl(url),
                title = title
            )
            articles.add(article)
        }
    }

    return articles
}
