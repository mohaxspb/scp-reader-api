package ru.kuchanov.scpreaderapi.controller

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
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
    private lateinit var langService: LangService

    @GetMapping("/{langEnum}/product/all")
    fun showAndroidProducts(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @AuthenticationPrincipal user: User?
    ) {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        articleParsingService.parseMostRecentArticlesForLang(lang, 2)
    }
}