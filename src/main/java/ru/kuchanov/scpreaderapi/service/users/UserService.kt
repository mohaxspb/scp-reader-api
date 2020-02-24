package ru.kuchanov.scpreaderapi.service.users

import org.springframework.security.core.userdetails.UserDetailsService
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjection
import ru.kuchanov.scpreaderapi.model.user.LeaderboardUserDto
import javax.transaction.Transactional

interface UserService : UserDetailsService {

    override fun loadUserByUsername(username: String): User?

    fun getByIdAsDto(id: Long): UserProjection?

    fun getById(id: Long): User?

    fun getByUsername(username: String): User?

    fun getByProviderId(id: String, provider: ScpReaderConstants.SocialProvider): User?

    fun getUsersByLangIdCount(langId: String): Long

    fun getLeaderboardUsersByLangWithOffsetAndLimitSortedByScore(offset: Int, limit: Int): List<LeaderboardUserDto>

    fun getUserPositionInLeaderboard(userId: Long, langId: String): Int

    @Transactional
    fun save(user: User): User

    @Transactional
    fun editAccount(userId: Long, name: String, avatarUrl: String): UserProjection
}
