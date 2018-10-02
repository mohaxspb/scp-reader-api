package ru.kuchanov.scpreaderapi.service.users

import ru.kuchanov.scpreaderapi.bean.users.Lang

interface LangService {
    fun findAll(): List<Lang>
    fun getById(id: String): Lang?
    fun update(lang: Lang): Lang

    fun insert(langs: List<Lang>): MutableList<Lang>?
}