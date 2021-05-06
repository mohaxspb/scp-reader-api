package ru.kuchanov.scpreaderapi.controller.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleCategoryToLangProjection
import ru.kuchanov.scpreaderapi.service.article.category.ArticleCategoryForLangService
import ru.kuchanov.scpreaderapi.service.users.LangService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.ARTICLE + "/" + ScpReaderConstants.Path.DOWNLOAD)
class MassArticleDownloadController @Autowired constructor(
        private val categoryForLangService: ArticleCategoryForLangService,
        private val langService: LangService
) {

    @GetMapping("{langEnum}/categories/all")
    fun all(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance
    ): List<ArticleCategoryToLangProjection> {
        val lang = langEnum.lang.let { langService.getById(it) ?: throw LangNotFoundException() }

        return categoryForLangService.findAllByLangId(langId = lang.id)
    }
}
