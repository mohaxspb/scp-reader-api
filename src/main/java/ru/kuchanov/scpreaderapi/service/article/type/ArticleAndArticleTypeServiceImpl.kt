package ru.kuchanov.scpreaderapi.service.article.type

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.types.ArticlesAndArticleTypes
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleTypeDto
import ru.kuchanov.scpreaderapi.repository.article.type.ArticleAndArticleTypeRepository

@Service
class ArticleAndArticleTypeServiceImpl @Autowired constructor(
        val repository: ArticleAndArticleTypeRepository,
        val articleTypeService: ArticleTypeService,
        val articleTypeForLangService: ArticleTypeForLangService
) : ArticleAndArticleTypeService {

    override fun save(articlesAndArticleTypes: ArticlesAndArticleTypes): ArticlesAndArticleTypes =
            repository.save(articlesAndArticleTypes)

    override fun getByArticleId(articleId: Long): ArticlesAndArticleTypes? =
            repository.findByArticleId(articleId)

    override fun getByArticleIdAndLangIdAsDto(articleId: Long, langId: String): ArticleTypeDto? =
            repository.findByArticleId(articleId)?.toDto(langId)

    private fun ArticlesAndArticleTypes.toDto(langId: String): ArticleTypeDto =
            ArticleTypeDto(
                    articleTypeId = articleTypeId,
                    enumValue = articleTypeService.getEnumValueById(articleTypeId),
                    titleForLang = articleTypeForLangService.getByArticleTypeIdAndLangId(articleTypeId, langId)?.title
            )
}
