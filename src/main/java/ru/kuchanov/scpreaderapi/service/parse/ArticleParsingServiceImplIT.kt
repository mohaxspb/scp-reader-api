package ru.kuchanov.scpreaderapi.service.parse

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import java.util.*


@Service
class ArticleParsingServiceImplIT : ArticleParsingServiceBase() {

    override fun getRatedArticlesUrl() = "/pagine-piu-votate/p/"

    override fun getRecentArticlesUrl() = "/system:recent-changes/p/"

    override fun getObjectArticlesUrls(): List<String> {
        return listOf(
                "/scp-series",
                "/scp-series-2",
                "/scp-series-3",
                "/scp-series-4"
        )
    }

    override fun parseForRecentArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        return listOf()
    }

    override fun parseForRatedArticles(lang: Lang, doc: Document): List<ArticleForLang> {
        val pageContent = doc.getElementById("page-content")
                ?: throw ScpParseException("parse error!")
        val listPagesBox = pageContent.getElementsByClass("list-pages-box").first()
                ?: throw ScpParseException("parse error!")
        val allArticles = listPagesBox.getElementsByTag("p").first().html()
        val arrayOfArticles = allArticles.split("<br>").toTypedArray()
        val articles: MutableList<ArticleForLang> = ArrayList()
        for (arrayItem in arrayOfArticles) {
            val currentDocument = Jsoup.parse(arrayItem)
            val aTag = currentDocument.getElementsByTag("a").first()
            val url: String = lang.siteBaseUrl + aTag.attr("href")
            val title = aTag.text()
            var rating = arrayItem.substring(arrayItem.indexOf("Voto: ") + "Voto: ".length)
            rating = rating.substring(0, rating.indexOf(", "))
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
}