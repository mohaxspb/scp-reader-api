package ru.kuchanov.scpreaderapi.service.parse.category

import org.jsoup.nodes.Document
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.users.Lang


@Service
class ArticleParsingServiceImplFR : ArticleParsingServiceBase() {

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

    override fun parseForRatedArticles(lang: Lang, doc: Document) =
            parseForRatedArticlesENStyle(
                    lang,
                    doc,
                    getArticleRatingStringDelimiter(),
                    getArticleRatingStringDelimiterEnd(),
                    1
            )

    override fun parseForObjectArticles(lang: Lang, doc: Document) =
            parseForObjectArticlesENStyle(lang, doc)

    override fun getArticleRatingStringDelimiter() = " ("

    override fun getArticleRatingStringDelimiterEnd() = ")"
}
