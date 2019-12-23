package ru.kuchanov.scpreaderapi.service.parse.category

import org.jsoup.nodes.Document
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.users.Lang


@Service
class ArticleParsingServiceImplES : ArticleParsingServiceBase() {

    override fun getRatedArticlesUrl() = "/paginas-mejor-puntuadas/p/"

    override fun getRecentArticlesUrl() = "/recientemente-creados/p/"

    override fun getObjectArticlesUrls() =
            listOf(
                    "/scp-series",
                    "/scp-series-2",
                    "/scp-series-3",
                    "/scp-series-4",
                    "/scp-series-5"
            )

    override fun parseForRecentArticles(lang: Lang, doc: Document) =
            parseForRecentArticlesENStyle(lang, doc)

    override fun parseForRatedArticles(lang: Lang, doc: Document) =
            parseForRatedArticlesENStyle(lang, doc, getArticleRatingStringDelimiter(), getArticleRatingStringDelimiterEnd())

    override fun parseForObjectArticles(lang: Lang, doc: Document) =
            parseForObjectArticlesENStyle(lang, doc)

    override fun getArticleRatingStringDelimiter() = " (+"

    override fun getArticleRatingStringDelimiterEnd() = ")"
}
