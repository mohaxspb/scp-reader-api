package ru.kuchanov.scpreaderapi.service.users

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.model.dto.user.LeaderboardUserProjection
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjection
import ru.kuchanov.scpreaderapi.model.user.LeaderboardUserDto
import ru.kuchanov.scpreaderapi.repository.users.UsersRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit


@Service
class ScpReaderUserServiceImpl @Autowired constructor(
        private val repository: UsersRepository,
        private val passwordEncoder: PasswordEncoder
) : ScpReaderUserService {

    override fun getById(id: Long): User? =
            repository.findByIdOrNull(id)

    override fun getByIdAsDto(id: Long) =
            repository.getByIdAsProjection(id)

    override fun getByUsername(username: String) =
            repository.findOneByUsername(username)

    override fun loadUserByUsername(username: String): User? =
            repository.findOneByUsername(username)

    override fun update(user: User): User =
            repository.saveAndFlush(user)

    override fun create(user: User): User =
            repository.saveAndFlush(user.copy(password = passwordEncoder.encode(user.password)))

    override fun getUsersByLangIdCount(langId: String): Long =
            repository.getUsersByLangCount(langId)

    override fun getLeaderboardUsersByLangWithOffsetAndLimitSortedByScore(
            offset: Int,
            limit: Int
    ): List<LeaderboardUserDto> =
            repository
                    .getLeaderboardUsersWithOffsetAndLimitSortedByScore(offset, limit)
                    .map { it.toDto() }

    override fun getByProviderId(id: String, provider: ScpReaderConstants.SocialProvider): User? =
            when (provider) {
                ScpReaderConstants.SocialProvider.GOOGLE -> repository.findOneByGoogleId(id)
                ScpReaderConstants.SocialProvider.FACEBOOK -> repository.findOneByFacebookId(id)
                ScpReaderConstants.SocialProvider.VK -> repository.findOneByVkId(id)
            }

    override fun getUserPositionInLeaderboard(userId: Long, langId: String): Int =
            repository.getUserPositionInLeaderboard(userId, langId)

    override fun editAccount(userId: Long, name: String, avatarUrl: String): UserProjection {
        if (avatarUrl.startsWith("http") || avatarUrl.startsWith("https")) {
            repository.editAccount(userId, name, avatarUrl)
            return repository.getByIdAsProjection(userId)!!
        } else {
            throw InvalidUrlException("$avatarUrl is not a valid url!")
        }
    }

    override fun getUserScoreById(userId: Long): Int =
            repository.getScoreById(userId)

    override fun disableAdsAndOfflineLimit(
            targetUserId: Long,
            disableAds: Boolean,
            disableOfflineLimit: Boolean,
            period: Int,
            timeUnit: ChronoUnit
    ): UserProjection {
        if (!disableAds && !disableOfflineLimit) {
            throw InvalidConditionException("One of disableAds or disableOfflineLimit must be true!")
        }

        val targetUser = getById(targetUserId) ?: throw UserNotFoundException()

        val endDate = LocalDateTime.ofInstant(Instant.now().plus(period.toLong(), timeUnit), ZoneOffset.UTC)
        repository.save(
                targetUser.apply {
                    if (disableAds) {
                        adsDisabledEndDate = endDate
                    }
                    if (disableOfflineLimit) {
                        offlineLimitDisabledEndDate = endDate
                    }
                }
        )

        return getByIdAsDto(targetUserId)!!
    }

    override fun countUsersCreatedBetweenDates(startDate: String, endDate: String): Int =
            repository.countUsersCreatedBetweenDates(startDate, endDate)

    fun LeaderboardUserProjection.toDto() =
            LeaderboardUserDto(id, avatar, fullName, score, levelNum, scoreToNextLevel, curLevelScore, numOfReadArticles)
}

@ResponseStatus(value = HttpStatus.CONFLICT)
class InvalidUrlException(override val message: String?) : RuntimeException(message)

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidConditionException(override val message: String?) : RuntimeException(message)
