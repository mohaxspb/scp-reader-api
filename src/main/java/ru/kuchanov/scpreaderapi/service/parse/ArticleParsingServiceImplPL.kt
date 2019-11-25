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

    override fun parseForRecentArticles(lang: Lang, doc: Document) =
            parseForRecentArticlesENStyle(lang, doc)

    override fun parseForRatedArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        val pageContent = doc.getElementById("page-content")
                ?: throw ScpParseException("parse error!")
        val listPagesBox = pageContent.getElementsByClass("list-pages-box").first()
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

    override fun getArticleRatingStringDelimiter() = "Ocena: "

    override fun getArticleRatingStringDelimiterEnd() = ", Komentarze"
}