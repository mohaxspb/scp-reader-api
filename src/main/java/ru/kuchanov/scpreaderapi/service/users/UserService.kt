package ru.kuchanov.scpreaderapi.service.users

import org.springframework.security.core.userdetails.UserDetailsService
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.user.LeaderboardUser
import javax.transaction.Transactional

interface UserService : UserDetailsService {
    fun findAll(): List<User>
    fun getById(id: Long): User
    fun getByUsername(username: String): User?
    fun update(user: User): User

    @Transactional
    fun insert(user: User): User

    fun insert(users: List<User>): List<User>

    fun getAllUsersByLangId(langId: String): List<User>

    fun getUsersByLangWithOffsetAndLimitSortedByScore(langId: String, offset: Int, limit: Int): List<User>
    fun getLeaderboardUsersByLangWithOffsetAndLimitSortedByScore(langId: String, offset: Int, limit: Int): List<LeaderboardUser>
}