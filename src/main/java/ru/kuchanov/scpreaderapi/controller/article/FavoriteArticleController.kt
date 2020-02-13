package ru.kuchanov.scpreaderapi.controller.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.favorite.FavoriteArticleByLang
import ru.kuchanov.scpreaderapi.bean.articles.favorite.FavoriteArticleByLangNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.dto.article.ReadOrFavoriteArticleToLangDto
import ru.kuchanov.scpreaderapi.service.article.favorite.FavoriteArticleForLangService
import ru.kuchanov.scpreaderapi.service.users.LangService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.ARTICLE + "/" + ScpReaderConstants.Path.FAVORITE)
class FavoriteArticleController @Autowired constructor(
        val favoriteArticleForLangService: FavoriteArticleForLangService,
        val langService: LangService
) {

    @GetMapping("{langEnum}/all")
    fun all(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "offset") offset: Int,
            @RequestParam(value = "limit") limit: Int,
            @AuthenticationPrincipal user: User
    ): List<ReadOrFavoriteArticleToLangDto> {
        val lang = langEnum.lang.let { langService.getById(it) ?: throw LangNotFoundException() }

        return favoriteArticleForLangService.findAllByUserIdAndLangId(
                userId = user.id!!,
                langId = lang.id,
                offset = offset,
                limit = limit
        )
    }

    @PostMapping("/add")
    fun add(
            @RequestParam(value = "articleToLangId") articleToLangId: Long,
            @AuthenticationPrincipal user: User
    ): FavoriteArticleByLang {
        val alreadySavedFavoriteArticle = favoriteArticleForLangService
                .findByArticleToLangIdAndUserId(
                        articleToLangId = articleToLangId,
                        userId = user.id!!
                )
        return if (alreadySavedFavoriteArticle == null) {
            favoriteArticleForLangService
                    .save(FavoriteArticleByLang(articleToLangId = articleToLangId, userId = user.id))
        } else {
            favoriteArticleForLangService.save(alreadySavedFavoriteArticle)
        }
    }

    @DeleteMapping("/delete")
    fun delete(
            @RequestParam(value = "articleToLangId") articleToLangId: Long,
            @AuthenticationPrincipal user: User
    ): FavoriteArticleByLang {
        val alreadySavedFavoriteArticle = favoriteArticleForLangService
                .findByArticleToLangIdAndUserId(
                        articleToLangId = articleToLangId,
                        userId = user.id!!
                )
        if (alreadySavedFavoriteArticle != null) {
            favoriteArticleForLangService.deleteById(alreadySavedFavoriteArticle.id!!)
            return alreadySavedFavoriteArticle
        } else {
            throw FavoriteArticleByLangNotFoundException()
        }
    }
}
