package ru.kuchanov.scpreaderapi.service.article.tags

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForArticleForLang
import ru.kuchanov.scpreaderapi.repository.article.tags.TagForArticleForLangRepository


@Suppress("unused")
@Service
class TagForArticleForLangServiceImpl : TagForArticleForLangService {

    @Autowired
    private lateinit var repository: TagForArticleForLangRepository

    override fun findAll() =
            repository.findAll().toList()

    override fun insert(data: TagForArticleForLang) =
            repository.save(data)

    override fun insert(data: List<TagForArticleForLang>) =
            repository.saveAll(data)

    override fun findOneById(id: Long) =
            repository.getOneById(id)

    override fun getOneByTagForLangIdAndArticleForLangIdOrCreate(
            tagForLangId: Long,
            articleForLangId: Long
    ): TagForArticleForLang =
            repository.getOneByTagForLangIdAndArticleForLangId(tagForLangId, articleForLangId)
                    ?: repository.save(
                            TagForArticleForLang(
                                    tagForLangId = tagForLangId,
                                    articleForLangId = articleForLangId
                            )
                    )
}