package ru.kuchanov.scpreaderapi.service.parse.category

import org.apache.http.util.TextUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ATTR_HREF
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ID_PAGE_CONTENT
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_A
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_P


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

        val innerPageContent = pageContent
                .getElementsByClass("white-paper")
                .first() ?: throw ScpParseException("Parse error. white-paper is null!", NullPointerException())

        innerPageContent.getElementsByClass("wp_sheet c_intro-toc").first()?.remove()
        //remove last block with no articles inside
        innerPageContent.getElementsByClass("wp_sheet justify hyphens c_list-box").last()?.remove()
        innerPageContent.getElementsByClass("panel-footer").remove()
        innerPageContent.getElementsByTag("h1").remove()
        innerPageContent.children().unwrap()

        //now we will remove all html code before tag h2,with id toc1
        val allHtml: String = innerPageContent.html()
//        log.error("allHtml: $allHtml")

        val arrayOfArticles = allHtml.split("<br>").toTypedArray()
        val articles: MutableList<ArticleForLang> = mutableListOf()
        for (arrayItem in arrayOfArticles) {
//            log.error(arrayItem)
            if (TextUtils.isEmpty(arrayItem.trim())) {
                continue
            }
            val arrayItemParsed = Jsoup.parse(arrayItem)
            //type of object
            val imageURL = arrayItemParsed.getElementsByTag(ParseConstants.TAG_IMG).first()?.attr(ParseConstants.ATTR_SRC)
            val type = if (imageURL != null) {
                getObjectTypeByImageUrl(imageURL)
            } else {
                log.error("imageURL is null!")
                log.error(arrayItem)
                log.error("imageURL is null!")
                ScpReaderConstants.ArticleTypeEnum.NONE
            }
            val url = arrayItemParsed.getElementsByTag(TAG_A).first().attr(ATTR_HREF)
            val title = arrayItemParsed.text()
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
