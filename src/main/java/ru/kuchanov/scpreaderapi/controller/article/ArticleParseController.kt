package ru.kuchanov.scpreaderapi.controller.article

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.settings.ServerSettings
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangService
import ru.kuchanov.scpreaderapi.service.parse.category.ArticleParsingServiceBase
import ru.kuchanov.scpreaderapi.service.settings.ServerSettingsService
import ru.kuchanov.scpreaderapi.service.users.LangService
import ru.kuchanov.scpreaderapi.utils.toBooleanOrNull


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.ARTICLE + "/" + ScpReaderConstants.Path.PARSE)
class ArticleParseController @Autowired constructor(
        val log: Logger,
        val serverSettingsService: ServerSettingsService,
        val articleParsingService: ArticleParsingServiceBase,
        val articleForLangService: ArticleForLangService,
        val langService: LangService
) {
    data class State(val state: String)

    class ParsingStartedResponse(
            state: String = "parsing started",
            status: HttpStatus = HttpStatus.ACCEPTED

    ) : ResponseEntity<State>(State(state), status)

    @GetMapping("/allLangsRecent")
    @Scheduled(
            /**
             * second, minute, hour, day, month, day of week
             */
//        cron = "*/30 * * * * *" //fi xme test
            cron = "0 0 * * * *"
    )
    fun parseRecentTask() {
        val hourlySyncTaskEnabledSettings = serverSettingsService.findByKey(ServerSettings.Key.HOURLY_SYNC_TASK_ENABLED.name)
        val hourlySyncTaskEnabled = hourlySyncTaskEnabledSettings?.value?.toBooleanOrNull()
        if (hourlySyncTaskEnabled == true && !articleParsingService.isDownloadAllRunning) {
            log.error("Start hourly parseRecentTask")
            articleParsingService.parseEverything(
                    maxPageCount = 1,
                    downloadRecent = true,
                    downloadObjects = false,
                    sendMail = false
            )
        } else {
            log.error("Start hourly parseRecentTask failed!")
            log.error("articleParsingService.isDownloadAllRunning: ${articleParsingService.isDownloadAllRunning}")
            log.error("hourlySyncTaskEnabled: $hourlySyncTaskEnabled")
        }
    }

    @GetMapping("/everything")
    fun updateEverything(
            @RequestParam(value = "maxPageCount") maxPageCount: Int?,
            @RequestParam(value = "processOnlyCount") processOnlyCount: Int?,
            @AuthenticationPrincipal user: User
    ): ParsingStartedResponse {
        return if (!articleParsingService.isDownloadAllRunning) {
            articleParsingService.parseEverything(
                    maxPageCount = maxPageCount,
                    processOnlyCount = processOnlyCount,
                    sendMail = true
            )
            ParsingStartedResponse()
        } else {
            ParsingStartedResponse(state = "Already running", status = HttpStatus.CONFLICT)
        }
    }

    @GetMapping("/{langEnum}/recent/all")
    fun updateRecentArticles(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "maxPageCount") maxPageCount: Int?,
            @RequestParam(value = "processOnlyCount") processOnlyCount: Int?,
            @RequestParam(value = "innerArticlesDepth") innerArticlesDepth: Int?,
            @AuthenticationPrincipal user: User
    ): ParsingStartedResponse {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        articleParsingService
                .getParsingRealizationForLang(lang)
                .parseMostRecentArticlesForLang(lang, maxPageCount, processOnlyCount, innerArticlesDepth)

        return ParsingStartedResponse()
    }

    @GetMapping("/{langEnum}/rated/all")
    fun updateRatedArticles(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "totalPageCount") totalPageCount: Int?,
            @RequestParam(value = "processOnlyCount") processOnlyCount: Int?,
            @RequestParam(value = "innerArticlesDepth") innerArticlesDepth: Int?,
            @AuthenticationPrincipal user: User
    ): ParsingStartedResponse {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        articleParsingService
                .getParsingRealizationForLang(lang)
                .parseMostRatedArticlesForLang(lang, totalPageCount, processOnlyCount, innerArticlesDepth)

        return ParsingStartedResponse()
    }

    @GetMapping("/{langEnum}/object/all")
    fun updateObjectArticles(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "maxPageCount") maxPageCount: Int?,
            @RequestParam(value = "processOnlyCount") processOnlyCount: Int?,
            @RequestParam(value = "innerArticlesDepth") innerArticlesDepth: Int?,
            @AuthenticationPrincipal user: User
    ): ParsingStartedResponse {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        articleParsingService
                .getParsingRealizationForLang(lang)
                .parseObjectsArticlesForLang(lang, maxPageCount, processOnlyCount, innerArticlesDepth)

        return ParsingStartedResponse()
    }

    @GetMapping("/{langEnum}/object/{concreteObject}")
    fun updateConcreteObjectArticles(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @PathVariable(value = "concreteObject") concreteObject: Int,
            @RequestParam(value = "processOnlyCount") processOnlyCount: Int?,
            @RequestParam(value = "offset") offset: Int?,
            @RequestParam(value = "innerArticlesDepth") innerArticlesDepth: Int?,
            @AuthenticationPrincipal user: User
    ): ParsingStartedResponse {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        val parsingService = articleParsingService.getParsingRealizationForLang(lang)
        val objectUrl = parsingService.getObjectArticlesUrls()[concreteObject]
        parsingService.parseConcreteObjectArticlesForLang(
                objectUrl,
                lang,
                offset,
                processOnlyCount,
                innerArticlesDepth
        )

        return ParsingStartedResponse()
    }

    @GetMapping("{langEnum}/parseArticleByUrlRelative")
    fun parseArticleByUrlRelativeAndLang(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "urlRelative") urlRelative: String,
            @RequestParam(value = "innerArticlesDepth", defaultValue = "0") innerArticlesDepth: Int = 0,
            @RequestParam(value = "printTextParts", defaultValue = "false") printTextParts: Boolean = false,
            @RequestParam(value = "async", defaultValue = "false") async: Boolean = false,
            @AuthenticationPrincipal user: User
    ): ResponseEntity<*> {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        val articleForLang = articleForLangService
                .getArticleForLangByUrlRelativeAndLang(urlRelative, lang.id)

        println("""
            parseArticleByUrlRelativeAndLang
            lang: ${lang.id}, 
            articleForLang.id: ${articleForLang?.id},
            article.id:${articleForLang?.articleId},
            printTextParts:$printTextParts
        """.trimIndent()
        )

        return if (async) {
            articleParsingService.parseArticleForLang(
                    urlRelative,
                    lang,
                    innerArticlesDepth,
                    printTextParts
            )
            ParsingStartedResponse("Parsing started for ArticleForLang id/title ${articleForLang?.id}/${articleForLang?.title}")
        } else {
            val savedArticle = articleParsingService.parseArticleForLangSync(
                    urlRelative,
                    lang,
                    innerArticlesDepth,
                    printTextParts
            )
            ResponseEntity(
                    savedArticle?.articleId?.let { articleForLangService.getOneByLangIdAndArticleIdAsDto(it, lang.id) },
                    HttpStatus.OK
            )
        }
    }
}