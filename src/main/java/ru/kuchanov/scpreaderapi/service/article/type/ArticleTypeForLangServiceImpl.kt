package ru.kuchanov.scpreaderapi.service.article.type

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.types.ArticleTypeForLang
import ru.kuchanov.scpreaderapi.repository.article.type.ArticleTypeForLangRepository

@Service
class ArticleTypeForLangServiceImpl @Autowired constructor(
        val repository: ArticleTypeForLangRepository
) : ArticleTypeForLangService {
    override fun getByArticleTypeIdAndLangId(articleTypeId: Long, langId: String): ArticleTypeForLang? =
            repository.getByArticleTypeIdAndLangId(articleTypeId, langId)
}
