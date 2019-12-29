package ru.kuchanov.scpreaderapi.service.article.read

import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticleByLang
import ru.kuchanov.scpreaderapi.model.dto.article.ReadOrFavoriteArticleToLangDto
import javax.transaction.Transactional

interface ReadArticleForLangService {

    @Transactional
    fun deleteById(id: Long)

    @Transactional
    fun save(article: ReadArticleByLang): ReadArticleByLang

    fun findByArticleToLangIdAndUserId(articleToLangId: Long, userId: Long): ReadArticleByLang?

    fun findAllByUserIdAndLangId(
            userId: Long,
            langId: String,
            offset: Int,
            limit: Int
    ): List<ReadOrFavoriteArticleToLangDto>
}
