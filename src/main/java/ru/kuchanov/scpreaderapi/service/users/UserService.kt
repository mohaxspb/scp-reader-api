package ru.kuchanov.scpreaderapi.service.users

import org.springframework.security.core.userdetails.UserDetailsService
import ru.kuchanov.scpreaderapi.bean.users.User

interface UserService : UserDetailsService {
    fun findAll(): List<User>
    fun getById(id: Long): User
    fun update(user: User): User

    fun insert(users: List<User>): MutableList<User>?

//    fun getAllByLangId(langId:String):List<User>
}