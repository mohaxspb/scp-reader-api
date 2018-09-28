package ru.kuchanov.scpreaderapi.service.users

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.users.UsersLangs
import ru.kuchanov.scpreaderapi.repository.users.UsersLangsRepository


@Service
class UsersLangServiceImpl : UsersLangsService {

    @Autowired
    private lateinit var repository: UsersLangsRepository

    override fun getByUserIdAndLangId(userId: Long, langId: String): UsersLangs? =
            repository.getOneByUserIdAndLangId(userId, langId)

    override fun insert(userLang: UsersLangs): UsersLangs = repository.save(userLang)

    override fun insert(usersLangs: List<UsersLangs>): List<UsersLangs> = repository.saveAll(usersLangs)
}