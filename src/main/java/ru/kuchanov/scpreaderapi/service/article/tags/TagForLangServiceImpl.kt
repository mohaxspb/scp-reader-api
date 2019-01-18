package ru.kuchanov.scpreaderapi.service.article.tags

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForLang
import ru.kuchanov.scpreaderapi.repository.article.tags.TagForLangRepository


@Suppress("unused")
@Service
class TagForLangServiceImpl : TagForLangService {

    @Autowired
    private lateinit var repository: TagForLangRepository

    override fun findOneById(id: Long) =
            repository.getOneById(id)

    override fun findOneByLangIdAndTitle(langId: String, title: String) =
            repository.findOneByLangIdAndTitle(langId, title)

    override fun getByLangIdAndTitleOrCreate(langId: String, title: String) =
            findOneByLangIdAndTitle(langId, title)
                    ?: insert(TagForLang(title = title, langId = langId))

    override fun findAll() =
            repository.findAll().toList()

    override fun insert(data: TagForLang): TagForLang =
            repository.save(data)

    override fun insert(data: List<TagForLang>) =
            repository.saveAll(data)
}