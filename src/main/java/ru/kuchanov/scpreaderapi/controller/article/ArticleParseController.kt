package ru.kuchanov.scpreaderapi.controller.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangService
import ru.kuchanov.scpreaderapi.service.parse.category.ArticleParsingServiceBase
import ru.kuchanov.scpreaderapi.service.users.LangService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.ARTICLE + "/" + ScpReaderConstants.Path.PARSE)
class ArticleParseController @Autowired constructor(
        val articleParsingService: ArticleParsingServiceBase,
        val articleForLangService: ArticleForLangService,
        val langService: LangService
) {

    @GetMapping("/{langEnum}/recent/all")
    fun updateRecentArticles(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "maxPageCount") maxPageCount: Int?,
            @RequestParam(value = "processOnlyCount") processOnlyCount: Int?,
            @RequestParam(value = "innerArticlesDepth") innerArticlesDepth: Int?,
            @AuthenticationPrincipal user: User?
    ): ResponseEntity<*> {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        articleParsingService
                .getParsingRealizationForLang(lang)
                .parseMostRecentArticlesForLang(lang, maxPageCount, processOnlyCount, innerArticlesDepth)

        return ResponseEntity(
                object {
                    @Suppress("unused")
                    val state = "parsing started"
                },
                HttpStatus.ACCEPTED
        )
    }

    @GetMapping("/{langEnum}/rated/all")
    fun updateRatedArticles(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "totalPageCount") totalPageCount: Int?,
            @RequestParam(value = "processOnlyCount") processOnlyCount: Int?,
            @RequestParam(value = "innerArticlesDepth") innerArticlesDepth: Int?,
            @AuthenticationPrincipal user: User?
    ): ResponseEntity<*> {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        articleParsingService
                .getParsingRealizationForLang(lang)
                .parseMostRatedArticlesForLang(lang, totalPageCount, processOnlyCount, innerArticlesDepth)

        return ResponseEntity(
                object {
                    @Suppress("unused")
                    val state = "parsing started"
                },
                HttpStatus.ACCEPTED
        )
    }

    @GetMapping("/{langEnum}/object/all")
    fun updateObjectArticles(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "maxPageCount") maxPageCount: Int?,
            @RequestParam(value = "processOnlyCount") processOnlyCount: Int?,
            @RequestParam(value = "innerArticlesDepth") innerArticlesDepth: Int?,
            @AuthenticationPrincipal user: User?
    ): ResponseEntity<*> {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        articleParsingService
                .getParsingRealizationForLang(lang)
                .parseObjectsArticlesForLang(lang, maxPageCount, processOnlyCount, innerArticlesDepth)

        return ResponseEntity(
                object {
                    @Suppress("unused")
                    val state = "parsing started"
                },
                HttpStatus.ACCEPTED
        )
    }

    @GetMapping("/{langEnum}/object/{concreteObject}")
    fun updateConcreteObjectArticles(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @PathVariable(value = "concreteObject") concreteObject: Int,
            @RequestParam(value = "processOnlyCount") processOnlyCount: Int?,
            @RequestParam(value = "innerArticlesDepth") innerArticlesDepth: Int?,
            @AuthenticationPrincipal user: User?
    ): ResponseEntity<*> {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        val parsingService = articleParsingService.getParsingRealizationForLang(lang)
        val objectUrl = parsingService.getObjectArticlesUrls()[concreteObject]
        parsingService.parseConcreteObjectArticlesForLang(objectUrl, lang, processOnlyCount, innerArticlesDepth)

        return ResponseEntity(
                object {
                    @Suppress("unused")
                    val state = "parsing started"
                },
                HttpStatus.ACCEPTED
        )
    }

    @GetMapping("{langEnum}/parseArticleByUrlRelative")
    fun parseArticleByUrlRelativeAndLang(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "urlRelative") urlRelative: String,
            @RequestParam(value = "printTextParts", defaultValue = "false") printTextParts: Boolean = false,
            @RequestParam(value = "async", defaultValue = "false") async: Boolean = false,
            @AuthenticationPrincipal user: User?
    ): ResponseEntity<*> {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        val articleForLang = articleForLangService.getArticleForLangByUrlRelativeAndLang(urlRelative, lang.id)

        println("parseArticleByUrlRelativeAndLang. lang: ${lang.id}, articleForLang.id: ${articleForLang?.id}, article.id: ${articleForLang?.articleId}, printTextParts: $printTextParts")

        if (async) {
            articleParsingService.parseArticleForLang(urlRelative, lang, printTextParts)
            return ResponseEntity(
                    object {
                        @Suppress("unused")
                        val state = "Parsing started for ArticleForLang id/title ${articleForLang?.id}/${articleForLang?.title}"
                    },
                    HttpStatus.ACCEPTED
            )
        } else {
            val savedArticle = articleParsingService.parseArticleForLangSync(urlRelative, lang, printTextParts)
            return ResponseEntity(
                    savedArticle?.articleId?.let { articleForLangService.getOneByLangIdAndArticleIdAsDto(it, lang.id) },
                    HttpStatus.OK
            )
        }
    }
}
