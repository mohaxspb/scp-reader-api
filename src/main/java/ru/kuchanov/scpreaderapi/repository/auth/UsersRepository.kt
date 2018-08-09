package ru.kuchanov.scpreaderapi.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.auth.User

interface UsersRepository : JpaRepository<User, Long> {
    fun findOneByMyUsername(username: String): User
}