package ru.kuchanov.scpreaderapi.service.users

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.repository.users.LangsRepository


@Service
class LangServiceImpl @Autowired constructor(
        val repository: LangsRepository
) : LangService {

    override fun findAll() = repository.findAll().toList()

    override fun getById(id: String) = repository.getOne(id) ?: throw UserNotFoundException()

    override fun insert(langs: List<Lang>): MutableList<Lang> = repository.saveAll(langs)

    override fun update(lang: Lang): Lang = repository.save(lang)
}
