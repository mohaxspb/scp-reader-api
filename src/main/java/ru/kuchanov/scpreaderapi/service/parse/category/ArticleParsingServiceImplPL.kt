package ru.kuchanov.scpreaderapi.service.parse.category

import org.jsoup.nodes.Document
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ATTR_HREF
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ID_PAGE_CONTENT
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_A
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_P
import java.sql.Timestamp


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
        println("start parsing rated articles for lang: $lang")
        val pageContent = doc.getElementById(ID_PAGE_CONTENT)
            ?: throw ScpParseException("parse error!")
        val listPagesBox = pageContent.getElementsByClass("list-pages-box").first()
            ?: throw ScpParseException("parse error!")
        val articlesDivs = listPagesBox.getElementsByClass("list-pages-item")
        val articles = mutableListOf<ArticleForLang>()
        for (element in articlesDivs) {
            val aTag = element.getElementsByTag(TAG_A).first()
            val url = aTag.attr(ATTR_HREF)
            val title = aTag.text()
            val pTag = element.getElementsByTag(TAG_P).first()
            var ratingString = pTag.text().substring(
                pTag.text().indexOf(getArticleRatingStringDelimiter()) + getArticleRatingStringDelimiter().length
            )
            ratingString = ratingString.substring(0, ratingString.indexOf(getArticleRatingStringDelimiterEnd()))
            val rating = ratingString.toInt()
            //parse date
            val updatedDateDelimiterString = "Ostatnio edytowane: "
            val indexOfUpdatedDate = pTag.text().indexOf(updatedDateDelimiterString)
            val indexOfUpdatedDateEnd = pTag.text().lastIndexOf(")")
            val updatedDateString =
                pTag.text().substring(indexOfUpdatedDate + updatedDateDelimiterString.length, indexOfUpdatedDateEnd)
            val updateDateValue = Timestamp(getDateFormatForLang().parse(updatedDateString).time)
            val article = ArticleForLang(
                langId = lang.id,
                urlRelative = lang.removeDomainFromUrl(url),
                title = title,
                rating = rating,
                updatedOnSite = updateDateValue
            )
            articles.add(article)
        }
        return articles
    }

    override fun parseForObjectArticles(lang: Lang, doc: Document) =
        parseForObjectArticlesENStyle(lang, doc, logger = log)

    override fun getArticleRatingStringDelimiter() = "Ocena: "

    override fun getArticleRatingStringDelimiterEnd() = ", Komentarze"
}
