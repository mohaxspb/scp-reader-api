package ru.kuchanov.scpreaderapi.service.article.tags

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.tags.Tag
import ru.kuchanov.scpreaderapi.repository.article.tags.TagsRepository


@Suppress("unused")
@Service
class TagServiceImpl : TagService {

    @Autowired
    private lateinit var repository: TagsRepository

    override fun findAll() =
            repository.findAll().toList()

    override fun insert(tag: Tag): Tag =
            repository.save(tag)

    override fun insert(tags: List<Tag>): List<Tag> =
            repository.saveAll(tags)

    override fun findOneById(id: Long): Tag? =
            repository.getOneById(id)
}