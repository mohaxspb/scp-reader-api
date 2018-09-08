package ru.kuchanov.scpreaderapi.service.users

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.users.UsersLangs
import ru.kuchanov.scpreaderapi.repository.users.UsersLangsRepository


@Service
class UsersLangServiceImpl : UsersLangsService {

    @Autowired
    private lateinit var repository: UsersLangsRepository

    override fun insert(usersLangs: List<UsersLangs>): MutableList<UsersLangs> = repository.saveAll(usersLangs)
}