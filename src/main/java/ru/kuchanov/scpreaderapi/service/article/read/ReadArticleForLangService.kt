package ru.kuchanov.scpreaderapi.service.article.read

import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticlesByLang
import javax.transaction.Transactional

interface ReadArticleForLangService {

    @Transactional
    fun deleteById(id: Long)

    @Transactional
    fun save(article: ReadArticlesByLang): ReadArticlesByLang

    fun findByArticleToLangIdAndUserId(articleToLangId: Long, userId: Long): ReadArticlesByLang?
}
