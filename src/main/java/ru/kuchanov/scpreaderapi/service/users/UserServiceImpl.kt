package ru.kuchanov.scpreaderapi.service.users

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.model.dto.user.LeaderboardUserProjection
import ru.kuchanov.scpreaderapi.model.user.LeaderboardUserDto
import ru.kuchanov.scpreaderapi.repository.users.UsersRepository


@Service
class UserServiceImpl @Autowired constructor(
        val repository: UsersRepository
) : UserService {

    override fun getByIdAsDto(id: Long) = repository.getByIdAsProjection(id)

    override fun getByUsername(username: String) = repository.findOneByUsername(username)

    override fun loadUserByUsername(username: String): User? = repository.findOneByUsername(username)

    override fun save(user: User): User = repository.save(user)

    override fun getUsersByLangIdCount(langId: String): Long = repository.getUsersByLangCount(langId)

    override fun getLeaderboardUsersByLangWithOffsetAndLimitSortedByScore(langId: String, offset: Int, limit: Int): List<LeaderboardUserDto> =
            repository
                    .getLeaderboardUsersWithOffsetAndLimitSortedByScore(offset, limit)
                    .map { it.toDto() }

    override fun getByProviderId(id: String, provider: ScpReaderConstants.SocialProvider) = when (provider) {
        ScpReaderConstants.SocialProvider.GOOGLE -> repository.findOneByGoogleId(id)
        ScpReaderConstants.SocialProvider.FACEBOOK -> repository.findOneByFacebookId(id)
        ScpReaderConstants.SocialProvider.VK -> repository.findOneByVkId(id)
    }

    override fun getUserPositionInLeaderboard(userId: Long, langId: String): Int =
            repository.getUserPositionInLeaderboard(userId, langId)

    fun LeaderboardUserProjection.toDto() =
            LeaderboardUserDto(id, avatar, fullName, score, levelNum, scoreToNextLevel, curLevelScore, numOfReadArticles)
}
