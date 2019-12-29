package ru.kuchanov.scpreaderapi.service.article.read

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticleByLang
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleForLangDto
import ru.kuchanov.scpreaderapi.repository.article.read.ReadArticlesForLangRepository
import javax.transaction.Transactional


@Service
class ReadArticleForLangServiceImpl @Autowired constructor(
        val repository: ReadArticlesForLangRepository
) : ReadArticleForLangService {

    override fun deleteById(id: Long) =
            repository.deleteById(id)

    @Transactional
    override fun save(article: ReadArticleByLang): ReadArticleByLang =
            repository.save(article)

    override fun findByArticleToLangIdAndUserId(articleToLangId: Long, userId: Long): ReadArticleByLang? =
            repository.findByArticleToLangIdAndUserId(articleToLangId, userId)

    override fun findAllByUserIdAndLangId(
            userId: Long,
            langId: String,
            offset: Int,
            limit: Int
    ): List<ArticleForLangDto> {
        TODO()
    }
}
