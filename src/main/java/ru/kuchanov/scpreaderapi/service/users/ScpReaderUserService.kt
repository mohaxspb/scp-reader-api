package ru.kuchanov.scpreaderapi.service.users

import org.springframework.security.core.userdetails.UserDetailsService
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjection
import ru.kuchanov.scpreaderapi.model.user.LeaderboardUserDto
import java.time.temporal.ChronoUnit
import javax.transaction.Transactional

interface ScpReaderUserService : UserDetailsService {

    override fun loadUserByUsername(username: String): User?

    fun getByIdAsDto(id: Long): UserProjection?

    fun getById(id: Long): User?

    fun getByUsername(username: String): User?

    fun getByProviderId(id: String, provider: ScpReaderConstants.SocialProvider): User?

    fun getUsersByLangIdCount(langId: String): Long

    fun getLeaderboardUsersByLangWithOffsetAndLimitSortedByScore(offset: Int, limit: Int): List<LeaderboardUserDto>

    fun getUserPositionInLeaderboard(userId: Long, langId: String): Int

    @Transactional
    fun update(user: User): User

    @Transactional
    fun create(user: User): User

    @Transactional
    fun editAccount(userId: Long, name: String, avatarUrl: String): UserProjection

    fun getUserScoreById(userId: Long): Int

    fun disableAdsAndOfflineLimit(
            targetUserId: Long,
            disableAds: Boolean,
            disableOfflineLimit: Boolean,
            period: Int,
            timeUnit: ChronoUnit
    ): UserProjection

    fun countUsersCreatedBetweenDates(startDate: String, endDate: String): Int
}
