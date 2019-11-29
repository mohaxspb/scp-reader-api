package ru.kuchanov.scpreaderapi.service.parse

import io.reactivex.Single
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

    override fun getMostRecentArticlesPageCountForLang(lang: Lang): Single<Int> =
            Single.just(0)

    override fun parseForRecentArticles(lang: Lang, doc: Document) =
        listOf<ArticleForLang>()

    override fun parseForRatedArticles(lang: Lang, doc: Document) =
            parseForRatedArticlesENStyle(lang, doc, getArticleRatingStringDelimiter(), getArticleRatingStringDelimiterEnd())

    override fun parseForObjectArticles(lang: Lang, doc: Document) =
            parseForObjectArticlesENStyle(lang, doc)

    override fun getArticleRatingStringDelimiter() = "Voto: "

    override fun getArticleRatingStringDelimiterEnd() = ","
}