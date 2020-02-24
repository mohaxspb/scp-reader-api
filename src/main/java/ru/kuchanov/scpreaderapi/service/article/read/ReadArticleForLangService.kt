package ru.kuchanov.scpreaderapi.service.article.read

import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticleByLang
import ru.kuchanov.scpreaderapi.model.dto.article.AddToReadResultDto
import ru.kuchanov.scpreaderapi.model.dto.article.ReadOrFavoriteArticleToLangDto
import javax.transaction.Transactional

interface ReadArticleForLangService {

    @Transactional
    fun deleteById(id: Long)

    @Transactional
    fun addArticleToRead(articleToLangId: Long, userId: Long, increaseScore: Boolean = true): AddToReadResultDto

    fun findByArticleToLangIdAndUserId(articleToLangId: Long, userId: Long): ReadArticleByLang?

    fun findAllByUserIdAndLangId(
            userId: Long,
            langId: String? = null,
            offset: Int,
            limit: Int
    ): List<ReadOrFavoriteArticleToLangDto>

    fun removeArticleFromRead(articleToLangId: Long, userId: Long): ReadArticleByLang
}
