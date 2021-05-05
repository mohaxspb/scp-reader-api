package ru.kuchanov.scpreaderapi.service.parse.category

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ATTR_HREF
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ATTR_SRC
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.CLASS_SPOILER
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ID_PAGE_CONTENT
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_A
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_BODY
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_IMG
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_P
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_TABLE
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


@Service
class ArticleParsingServiceImplPT : ArticleParsingServiceBase() {

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

    override fun parseForRecentArticles(lang: Lang, doc: Document) =
            parseForRecentArticlesENStyle(lang, doc)

    @Suppress("DuplicatedCode")
    override fun parseForRatedArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        println("start parsing rated articles for lang: $lang")
        val pageContent = doc.getElementById(ID_PAGE_CONTENT)
                ?: throw ScpParseException("$ID_PAGE_CONTENT is null!", NullPointerException())
        val listPagesBox = pageContent.getElementsByClass("panel-body").last()
                ?: throw ScpParseException("parse error!")
        val articlesDivs = listPagesBox.getElementsByClass("list-pages-item")
        val articles = mutableListOf<ArticleForLang>()
        for (element in articlesDivs) {
            val aTag = element.getElementsByTag(TAG_A).first()
            val url = aTag.attr(ATTR_HREF)
            val title = aTag.text()
            val pTag = element.getElementsByTag(TAG_P).first()
            var ratingString = pTag.text().substring(pTag.text().indexOf(getArticleRatingStringDelimiter()) + getArticleRatingStringDelimiter().length)
            ratingString = ratingString.substring(0, ratingString.indexOf(getArticleRatingStringDelimiterEnd()))
            val rating = ratingString.toInt()
            val article = ArticleForLang(
                    langId = lang.id,
                    urlRelative = lang.removeDomainFromUrl(url),
                    title = title,
                    rating = rating
            )
            articles.add(article)
        }
        return articles
    }

    @Suppress("DuplicatedCode")
    override fun parseForObjectArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        val pageContent: Element = doc.getElementById(ID_PAGE_CONTENT)
                ?: throw ScpParseException("Parse error. $ID_PAGE_CONTENT is null!", NullPointerException())

        //parse
        val innerPageContent = pageContent
                .getElementsByClass("content-panel standalone series")
                .first()

        innerPageContent.getElementsByClass("list-pages-box").first()?.remove()
        innerPageContent.getElementsByClass(CLASS_SPOILER).first()?.remove()
        innerPageContent.getElementsByTag(TAG_TABLE).first()?.remove()
        innerPageContent.getElementById("toc0")?.remove()
        val aWithNameAttr2 = innerPageContent.getElementsByTag(TAG_A)
        if (aWithNameAttr2 != null) {
            for (element in aWithNameAttr2) {
                if (element.hasAttr("name")) {
                    element.remove()
                }
            }
        }

        //now we will remove all html code before tag h2,with id toc1
        var allHtml: String = pageContent.html()
        var indexOfh2WithIdToc1 = allHtml.indexOf("<h1 id=\"toc2\">")
        if (indexOfh2WithIdToc1 == -1) {
            indexOfh2WithIdToc1 = allHtml.indexOf("<h1 id=\"toc3\">")
        }
        var indexOfHr = allHtml.indexOf("<hr>")
        //for other objects filials there is no HR tag at the end...

        //for other objects filials there is no HR tag at the end...
        if (indexOfHr < indexOfh2WithIdToc1) {
            indexOfHr = allHtml.indexOf("<p style=\"text-align: center;\">= = = =</p>")
        }
        if (indexOfHr < indexOfh2WithIdToc1) {
            indexOfHr = allHtml.length
        }
        allHtml = allHtml.substring(indexOfh2WithIdToc1, indexOfHr)

        val document = Jsoup.parse(allHtml)

        document.getElementsByTag("h1")?.remove()
        document.getElementsByTag(TAG_P)?.remove()

        val allH2Tags = document.getElementsByTag("h2")
        for (h2Tag in allH2Tags) {
            val brTag = Element(Tag.valueOf("br"), "")
            h2Tag.replaceWith(brTag)
        }

        val allArticles = document.getElementsByTag(TAG_BODY).first().html()
        val arrayOfArticles = allArticles.split("<br>").toTypedArray()
        val articles = mutableListOf<ArticleForLang>()
        for (arrayItem in arrayOfArticles) {
            val arrayItemAsDocument = Jsoup.parse(arrayItem)

            val url = arrayItemAsDocument.getElementsByTag(TAG_A).first().attr(ATTR_HREF)
            val title = arrayItemAsDocument.text()

            //type of object
            val imageURL = arrayItemAsDocument
                    .getElementsByTag(TAG_IMG)
                    .ifEmpty { null }
                    ?.first()
                    ?.attr(ATTR_SRC)
//            println("title: $title, imageURL: $imageURL, ${imageURL?.let{URLDecoder.decode(it, StandardCharsets.UTF_8)}}")

            val type = imageURL?.let { getObjectTypeByImageUrl(URLDecoder.decode(it, StandardCharsets.UTF_8)) }

            val article = ArticleForLang(
                    langId = lang.id,
                    urlRelative = lang.removeDomainFromUrl(url),
                    title = title,
                    articleTypeEnumEnumValue = type
            )
            articles.add(article)
        }

        return articles
    }

    override fun getArticleRatingStringDelimiter() = "avaliação "

    override fun getArticleRatingStringDelimiterEnd() = "."

    override fun getObjectTypeByImageUrl(imageURL: String): ScpReaderConstants.ArticleTypeEnum =
            when (imageURL) {
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-6/na.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-5/na.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-4/na.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-3/na.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-2/na.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series/na.png" ->
                    ScpReaderConstants.ArticleTypeEnum.NEUTRAL_OR_NOT_ADDED

                "http://scp-pt-br.wdfiles.com/local--files/scp-series-6/seguro.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-5/seguro.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-4/seguro.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-3/seguro.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-2/seguro.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series/seguro.png" ->
                    ScpReaderConstants.ArticleTypeEnum.SAFE

                "http://scp-pt-br.wdfiles.com/local--files/scp-series-6/euclídeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-6/eucl%C3%ADdeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-5/euclídeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-5/eucl%C3%ADdeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-4/euclídeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-4/eucl%C3%ADdeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-3/euclídeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-3/eucl%C3%ADdeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-2/euclídeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-2/eucl%C3%ADdeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series/eucl%C3%ADdeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series/euclídeo.png" ->
                    ScpReaderConstants.ArticleTypeEnum.EUCLID

                "http://scp-pt-br.wdfiles.com/local--files/scp-series-6/keter.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-5/keter.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-4/keter.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-3/keter.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-2/keter.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series/keter.png" ->
                    ScpReaderConstants.ArticleTypeEnum.KETER

                "http://scp-pt-br.wdfiles.com/local--files/scp-series-6/thaumiel.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-5/thaumiel.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-4/thaumiel.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-3/thaumiel.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-2/thaumiel.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series/thaumiel.png" ->
                    ScpReaderConstants.ArticleTypeEnum.THAUMIEL

                else -> ScpReaderConstants.ArticleTypeEnum.NONE
            }
}
