package ru.kuchanov.scpreaderapi.service.article.error

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.error.ArticleParseError
import ru.kuchanov.scpreaderapi.repository.article.error.ArticleParseErrorRepository

@Service
class ArticleParseErrorServiceImpl @Autowired constructor(
        val repository: ArticleParseErrorRepository
) : ArticleParseErrorService {

    override fun save(error: ArticleParseError): ArticleParseError =
            repository.save(error)

    override fun findAllByLangIdAndUrlRelative(langId: String, urlRelative: String): List<ArticleParseError> =
            repository.findAllByLangIdAndUrlRelative(langId, urlRelative)
}
