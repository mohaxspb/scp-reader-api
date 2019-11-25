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
class ArticleParsingServiceImplES : ArticleParsingServiceBase() {

    override fun getRatedArticlesUrl() = "/paginas-mejor-puntuadas/p/"

    override fun getRecentArticlesUrl() = "/recientemente-creados/p/"

    override fun getObjectArticlesUrls(): List<String> {
        return listOf(
                "/serie-scp-1",
                "/serie-scp-2",
                "/serie-scp-3",
                "/serie-scp-4",
                "/serie-scp-5"
        )
    }

    override fun parseForRecentArticles(lang: Lang, doc: Document) =
            parseForRecentArticlesENStyle(lang, doc)

    override fun parseForRatedArticles(lang: Lang, doc: Document) =
            parseForRatedArticlesENStyle(lang, doc, getArticleRatingStringDelimiter(), getArticleRatingStringDelimiterEnd())

    override fun getArticleRatingStringDelimiter() = " (+"

    override fun getArticleRatingStringDelimiterEnd() = ")"

    override fun getArticleFromApi(url: String, lang: Lang): ArticleForLang? {

        //TODO check commentsUrl for ES site
        /*
        18:31:24.054 error in articles parsing: /hub-del-sarkicismo
java.lang.NullPointerException: null
         */
        return super.getArticleFromApi(url, lang)
    }

}