package ru.kuchanov.scpreaderapi.controller.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticleByLang
import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticleByLangAlreadyExistsException
import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticleByLangNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.dto.article.ReadOrFavoriteArticleToLangDto
import ru.kuchanov.scpreaderapi.service.article.read.ReadArticleForLangService
import ru.kuchanov.scpreaderapi.service.users.LangService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.ARTICLE + "/" + ScpReaderConstants.Path.READ)
class ReadArticleController @Autowired constructor(
        val readArticleForLangService: ReadArticleForLangService,
        val langService: LangService
) {

    @GetMapping("all")
    fun all(
            @RequestParam(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance?,
            @RequestParam(value = "offset") offset: Int,
            @RequestParam(value = "limit") limit: Int,
            @AuthenticationPrincipal user: User
    ): List<ReadOrFavoriteArticleToLangDto> {
        val lang = langEnum?.lang?.let { langService.getById(it) ?: throw LangNotFoundException() }

        return readArticleForLangService.findAllByUserIdAndLangId(
                userId = user.id!!,
                langId = lang?.id,
                offset = offset,
                limit = limit
        )
    }

    @PostMapping("/add")
    fun add(
            @RequestParam(value = "articleToLangId") articleToLangId: Long,
            @AuthenticationPrincipal user: User
    ): ReadArticleByLang {
        val alreadySavedReadArticle = readArticleForLangService.findByArticleToLangIdAndUserId(
                articleToLangId = articleToLangId,
                userId = user.id!!
        )
        if (alreadySavedReadArticle == null) {
            return readArticleForLangService.save(ReadArticleByLang(articleToLangId = articleToLangId, userId = user.id))
        } else {
            throw ReadArticleByLangAlreadyExistsException()
        }
    }

    @DeleteMapping("/delete")
    fun delete(
            @RequestParam(value = "articleToLangId") articleToLangId: Long,
            @AuthenticationPrincipal user: User
    ): ReadArticleByLang {
        val alreadySavedReadArticle = readArticleForLangService.findByArticleToLangIdAndUserId(
                articleToLangId = articleToLangId,
                userId = user.id!!
        )
        if (alreadySavedReadArticle != null) {
            readArticleForLangService.deleteById(alreadySavedReadArticle.id!!)
            return alreadySavedReadArticle
        } else {
            throw ReadArticleByLangNotFoundException()
        }
    }
}
