package ru.kuchanov.scpreaderapi.service.users

import ru.kuchanov.scpreaderapi.bean.users.UsersLangs

interface UsersLangsService {
    fun insert(usersLangs: List<UsersLangs>): MutableList<UsersLangs>?
}