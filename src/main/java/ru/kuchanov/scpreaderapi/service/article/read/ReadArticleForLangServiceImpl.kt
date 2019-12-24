package ru.kuchanov.scpreaderapi.service.article.read

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticlesByLang
import ru.kuchanov.scpreaderapi.repository.article.read.ReadArticlesForLangRepository
import javax.transaction.Transactional


@Service
class ReadArticleForLangServiceImpl @Autowired constructor(
        val repository: ReadArticlesForLangRepository
) : ReadArticleForLangService {

    override fun deleteById(id: Long) =
            repository.deleteById(id)

    @Transactional
    override fun save(article: ReadArticlesByLang): ReadArticlesByLang =
            repository.save(article)

    override fun findByArticleToLangIdAndUserId(articleToLangId: Long, userId: Long): ReadArticlesByLang? =
            repository.findByArticleToLangIdAndUserId(articleToLangId, userId)
}
