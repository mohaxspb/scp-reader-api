package ru.kuchanov.scpreaderapi.service.parse

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
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
        val pageContent = doc.getElementById("page-content")
                ?: throw ScpParseException("parse error!")
        val listPagesBox = pageContent.getElementsByClass("panel-body").last()
                ?: throw ScpParseException("parse error!")
        val articlesDivs = listPagesBox.getElementsByClass("list-pages-item")
        val articles = mutableListOf<ArticleForLang>()
        for (element in articlesDivs) {
            val aTag = element.getElementsByTag("a").first()
            val url: String = lang.siteBaseUrl + aTag.attr("href")
            val title = aTag.text()
            val pTag = element.getElementsByTag("p").first()
            var ratingString = pTag.text().substring(pTag.text().indexOf(getArticleRatingStringDelimiter()) + getArticleRatingStringDelimiter().length)
            ratingString = ratingString.substring(0, ratingString.indexOf(getArticleRatingStringDelimiterEnd()))
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

    @Suppress("DuplicatedCode")
    override fun parseForObjectArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        val pageContent: Element = doc.getElementById("page-content")
                ?: throw ScpParseException("Parse error. \"page-content is null!\"")

        //parse
        val innerPageContent = pageContent
                .getElementsByClass("content-panel standalone series")
                .first()

        innerPageContent.getElementsByClass("list-pages-box").first()?.remove()
        innerPageContent.getElementsByClass("collapsible-block").first()?.remove()
        innerPageContent.getElementsByTag("table").first()?.remove()
        innerPageContent.getElementById("toc0")?.remove()
        val aWithNameAttr2 = innerPageContent.getElementsByTag("a")
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
        document.getElementsByTag("p")?.remove()

        val allH2Tags = document.getElementsByTag("h2")
        for (h2Tag in allH2Tags) {
            val brTag = Element(Tag.valueOf("br"), "")
            h2Tag.replaceWith(brTag)
        }

        val allArticles = document.getElementsByTag("body").first().html()
        val arrayOfArticles = allArticles.split("<br>").toTypedArray()
        val articles = mutableListOf<ArticleForLang>()
        for (arrayItem in arrayOfArticles) {
            val arrayItemAsDocument = Jsoup.parse(arrayItem)

            val url = arrayItemAsDocument
                    .getElementsByTag("a")
                    .first()
                    .attr("href")
                    .replace(lang.siteBaseUrl, "")
            val title = arrayItemAsDocument.text()

            //type of object
            val imageURL = arrayItemAsDocument
                    .getElementsByTag("img")
                    .ifEmpty { null }
                    ?.first()
                    ?.attr("src")
//            println("title: $title, imageURL: $imageURL, ${imageURL?.let{URLDecoder.decode(it, StandardCharsets.UTF_8)}}")

            val type = imageURL?.let { getObjectTypeByImageUrl(URLDecoder.decode(it, StandardCharsets.UTF_8)) }

            val article = ArticleForLang(
                    langId = lang.id,
                    urlRelative = url,
                    title = title,
                    articleTypeEnumEnumValue = type
            )
            articles.add(article)
        }

        return articles
    }

    override fun getArticleRatingStringDelimiter() = "avaliação "

    override fun getArticleRatingStringDelimiterEnd() = "."

    override fun getArticleFromApi(url: String, lang: Lang): ArticleForLang? {
        //TODO check commentsUrl parsing (http://scp-pt-br.wikidot.comjavascript:;)

        return super.getArticleFromApi(url, lang)
    }

    override fun getObjectTypeByImageUrl(imageURL: String): ScpReaderConstants.ArticleTypeEnum =
            when (imageURL) {
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-5/na.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-4/na.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-3/na.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-2/na.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series/na.png" ->
                    ScpReaderConstants.ArticleTypeEnum.NEUTRAL_OR_NOT_ADDED

                "http://scp-pt-br.wdfiles.com/local--files/scp-series-5/seguro.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-4/seguro.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-3/seguro.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-2/seguro.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series/seguro.png" ->
                    ScpReaderConstants.ArticleTypeEnum.SAFE

                "http://scp-pt-br.wdfiles.com/local--files/scp-series-5/euclídeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-4/euclídeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-3/euclídeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-2/euclídeo.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series/euclídeo.png" ->
                    ScpReaderConstants.ArticleTypeEnum.EUCLID

                "http://scp-pt-br.wdfiles.com/local--files/scp-series-5/keter.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-4/keter.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-3/keter.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-2/keter.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series/keter.png" ->
                    ScpReaderConstants.ArticleTypeEnum.KETER

                "http://scp-pt-br.wdfiles.com/local--files/scp-series-5/thaumiel.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-4/thaumiel.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-3/thaumiel.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series-2/thaumiel.png",
                "http://scp-pt-br.wdfiles.com/local--files/scp-series/thaumiel.png" ->
                    ScpReaderConstants.ArticleTypeEnum.THAUMIEL

                else -> ScpReaderConstants.ArticleTypeEnum.NONE
            }
}
