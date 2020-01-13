package ru.kuchanov.scpreaderapi.service.article.tags

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForLang
import ru.kuchanov.scpreaderapi.repository.article.tags.TagForLangRepository


@Service
class TagForLangServiceImpl @Autowired constructor(
        val repository: TagForLangRepository
) : TagForLangService {

    override fun findOneById(id: Long) =
            repository.getOneById(id)

    override fun findOneByLangIdAndTitle(langId: String, title: String) =
            repository.findOneByLangIdAndTitle(langId, title)

    override fun findAll() =
            repository.findAll().toList()

    override fun insert(data: TagForLang): TagForLang =
            repository.save(data)
}
