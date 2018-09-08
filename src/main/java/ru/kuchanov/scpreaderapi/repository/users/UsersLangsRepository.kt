package ru.kuchanov.scpreaderapi.repository.users

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.users.UsersLangs

interface UsersLangsRepository : JpaRepository<UsersLangs, String> {
    fun getOneByUserIdAndLangId(userId: Long, langId: String): UsersLangs?
}