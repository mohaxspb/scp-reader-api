package ru.kuchanov.scpreaderapi.service.users

import ru.kuchanov.scpreaderapi.bean.users.Lang

interface LangService {

    fun findAll(): List<Lang>

    fun getById(id: String): Lang?
}
