package ru.kuchanov.scpreaderapi.repository.users

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.users.User

interface UsersRepository : JpaRepository<User, Long> {
    fun findOneByMyUsername(username: String): User
}