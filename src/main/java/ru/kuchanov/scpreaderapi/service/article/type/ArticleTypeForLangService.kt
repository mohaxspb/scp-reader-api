package ru.kuchanov.scpreaderapi.service.article.type

import ru.kuchanov.scpreaderapi.bean.articles.types.ArticleTypeForLang

interface ArticleTypeForLangService {

    fun getByArticleTypeIdAndLangId(articleTypeId: Long, langId: String): ArticleTypeForLang?
}
