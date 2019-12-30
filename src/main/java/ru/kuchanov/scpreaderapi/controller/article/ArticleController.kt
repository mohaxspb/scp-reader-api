package ru.kuchanov.scpreaderapi.controller.article

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLangNotFoundException
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForLangNotFoundException
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleToLangDto
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.category.ArticleCategoryForLangService
import ru.kuchanov.scpreaderapi.service.article.category.ArticleCategoryService
import ru.kuchanov.scpreaderapi.service.article.text.TextPartService
import ru.kuchanov.scpreaderapi.service.users.LangService


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.ARTICLE)
class ArticleController @Autowired constructor(
        val articleForLangService: ArticleForLangService,
        val langService: LangService,
        val categoryForLangService: ArticleCategoryForLangService,
        val categoryService: ArticleCategoryService,
        val textPartService: TextPartService
) {

    @GetMapping("/{langEnum}/recent")
    fun showRecentArticles(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "offset") offset: Int,
            @RequestParam(value = "limit") limit: Int
    ) =
            articleForLangService.getMostRecentArticlesForLang(langEnum.lang, offset, limit)

    @GetMapping("/{langEnum}/rated")
    fun showRatedArticles(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "offset") offset: Int,
            @RequestParam(value = "limit") limit: Int
    ) =
            articleForLangService.getMostRatedArticlesForLang(langEnum.lang, offset, limit)

    @GetMapping("/{langEnum}/category/{categoryId}/")
    fun getArticlesByCategoryForLang(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @PathVariable(value = "categoryId") categoryId: Long
    ): List<ArticleToLangDto> {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        val category = categoryService.getById(categoryId)
                ?: throw ArticleCategoryNotFoundException()
        val articleCategoryToLang = categoryForLangService.findByLangIdAndArticleCategoryId(
                langId = lang.id,
                articleCategoryId = category.id!!
        )
                ?: throw ArticleCategoryForLangNotFoundException()
        return articleForLangService.findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLang.id!!)
    }

    @GetMapping("{langEnum}/{id}")
    fun showArticleForLangAndId(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @PathVariable(value = "id") articleId: Long
    ) =
            articleForLangService.getOneByLangAndArticleId(articleId, langEnum.lang)
                    ?: throw ArticleForLangNotFoundException()

    @GetMapping("{langEnum}/{id}/full")
    fun showArticleForIdAndLangIdFull(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @PathVariable(value = "id") articleId: Long
    ): ArticleToLangDto {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        return articleForLangService.getOneByLangIdAndArticleIdAsDto(articleId, lang.id)
                ?: throw ArticleForLangNotFoundException()
    }

    @DeleteMapping("{langEnum}/{articleId}/delete")
    fun deleteArticleTextPartsByLangAndArticleId(
            @PathVariable(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @PathVariable(value = "articleId") articleId: Long
    ): Boolean {
        val lang = langService.getById(langEnum.lang) ?: throw LangNotFoundException()
        val articleToLangId = articleForLangService.getOneByLangAndArticleId(articleId, lang.id)?.id
                ?: throw ArticleForLangNotFoundException()
        textPartService.deleteByArticleToLangId(articleToLangId)
        return true
    }

    @GetMapping("random")
    fun getRandomArticle(@RequestParam(value = "langEnum") langEnum: ScpReaderConstants.Firebase.FirebaseInstance?):ArticleToLangDto {
        val lang = langEnum?.lang?.let { langService.getById(it) ?: throw LangNotFoundException() }
        return articleForLangService.getRandomArticle(lang?.id)
    }

}
