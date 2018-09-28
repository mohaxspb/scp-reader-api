package ru.kuchanov.scpreaderapi.service.users

import ru.kuchanov.scpreaderapi.bean.users.UsersLangs
import javax.transaction.Transactional

interface UsersLangsService {
    @Transactional
    fun insert(userLang: UsersLangs): UsersLangs

    fun insert(usersLangs: List<UsersLangs>): List<UsersLangs>
    fun getByUserIdAndLangId(userId: Long, langId: String): UsersLangs?
}