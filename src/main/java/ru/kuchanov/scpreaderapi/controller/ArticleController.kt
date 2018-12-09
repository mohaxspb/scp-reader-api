package ru.kuchanov.scpreaderapi.controller

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLangNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangService
import ru.kuchanov.scpreaderapi.service.parse.ArticleParsingService
import ru.kuchanov.scpreaderapi.service.users.LangService


@RestController
@RequestMapping("/${ScpReaderConstants.Path.ARTICLE}")
class ArticleController {

    @Suppress("unused")
    @Autowired
    private lateinit var log: Logger

    @Autowired
    private lateinit var articleParsingService: ArticleParsingService

    @Autowired
    private lateinit var articleForLangService: ArticleForLangService

    @Autowired
    private lateinit var langService: LangService

    @GetMapping("/{langEnum}/recent/all")
    fun updateRecentArticles(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @AuthenticationPrincipal user: User?
    ) {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        articleParsingService.parseMostRecentArticlesForLang(lang, 1)
    }

    @GetMapping("/{langEnum}/recent")
    fun showRecentArticles(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "offset") offset: Int,
            @RequestParam(value = "limit") limit: Int,
            @AuthenticationPrincipal user: User?
    ) = articleForLangService.getMostRecentArticlesForLang(langEnum.lang, offset, limit)

    @GetMapping("{langEnum}/{id}")
    fun showArticleForLangAndId(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @PathVariable(value = "id") articleId: Long
    ) = articleForLangService.getOneByLangAndArticleId(articleId, langEnum.lang)
            ?: throw ArticleForLangNotFoundException()
}