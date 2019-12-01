package ru.kuchanov.scpreaderapi.service.article.type

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.types.ArticlesAndArticleTypes
import ru.kuchanov.scpreaderapi.repository.article.type.ArticleAndArticleTypeRepository

@Service
class ArticleAndArticleTypeServiceImpl @Autowired constructor(
        val repository: ArticleAndArticleTypeRepository
) : ArticleAndArticleTypeService {

    override fun save(articlesAndArticleTypes: ArticlesAndArticleTypes): ArticlesAndArticleTypes =
            repository.save(articlesAndArticleTypes)

    override fun getByArticleId(articleId: Long): ArticlesAndArticleTypes? =
            repository.findByArticleId(articleId)
}
