package ru.kuchanov.scpreaderapi.repository.article.type

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.articles.types.ArticleTypeForLang

interface ArticleTypeForLangRepository : JpaRepository<ArticleTypeForLang, Long> {

    fun getByArticleTypeIdAndLangId(articleTypeId: Long, langId: String): ArticleTypeForLang?
}
