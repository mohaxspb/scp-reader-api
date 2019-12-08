package ru.kuchanov.scpreaderapi.service.article.category

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategory
import ru.kuchanov.scpreaderapi.repository.article.category.ArticleCategoryRepository

@Service
class ArticleCategoryServiceImpl @Autowired constructor(
        val repository: ArticleCategoryRepository
) : ArticleCategoryService {

    override fun getByDefaultTitle(defaultTitle: String): ArticleCategory? =
            repository.findByDefaultTitle(defaultTitle)

    override fun getById(categoryId: Long): ArticleCategory? =
            repository.findByIdOrNull(categoryId)
}
