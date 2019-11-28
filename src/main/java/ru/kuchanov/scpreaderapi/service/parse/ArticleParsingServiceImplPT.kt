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

    override fun getArticleRatingStringDelimiter() = "avaliação "

    override fun getArticleRatingStringDelimiterEnd() = "."

    override fun getArticleFromApi(url: String, lang: Lang): ArticleForLang? {
        //TODO check commentsUrl parsing (http://scp-pt-br.wikidot.comjavascript:;)
        
        return super.getArticleFromApi(url, lang)
    }


}