package ru.kuchanov.scpreaderapi.service.auth

import org.springframework.security.core.userdetails.UserDetailsService
import ru.kuchanov.scpreaderapi.bean.auth.User

interface UserService: UserDetailsService {
    fun findAll(): List<User>
    fun getById(id: Long): User
    fun update(user: User): User
}