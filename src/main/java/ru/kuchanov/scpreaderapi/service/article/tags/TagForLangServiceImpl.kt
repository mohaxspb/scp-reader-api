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

    override fun findAll() =
            repository.findAll().toList()

    override fun insert(data: TagForLang) =
            repository.save(data)

    override fun insert(data: List<TagForLang>) =
            repository.saveAll(data)

    override fun findOneById(id: Long) =
            repository.getOneById(id)
}