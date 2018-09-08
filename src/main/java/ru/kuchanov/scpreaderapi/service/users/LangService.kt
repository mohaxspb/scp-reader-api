package ru.kuchanov.scpreaderapi.service.users

import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.bean.users.User

interface LangService {
    fun findAll(): List<Lang>
    fun getById(id: String): Lang
    fun update(user: Lang): Lang

    fun insert(langs: List<Lang>): MutableList<Lang>?

    fun getAllUsersByLangId(langId: String): List<User>
}