package ru.kuchanov.scpreaderapi.repository.users

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.bean.users.User

interface LangsRepository : JpaRepository<Lang, String> {
    fun findOneById(id: String): Lang

    @Query("SELECT u from User u JOIN UsersLangs ul ON u.id = ul.userId WHERE ul.langId = :langId")
    fun getUsersByLang(langId: String): List<User>
}