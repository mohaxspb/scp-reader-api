package ru.kuchanov.scpreaderapi.service.users

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.model.user.LeaderboardUser
import ru.kuchanov.scpreaderapi.repository.users.UsersRepository
import javax.persistence.EntityManager


@Service
class UserServiceImpl : UserService {

    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var repository: UsersRepository

    override fun findAll() = repository.findAll().toList()

    override fun getById(id: Long) = repository.getOne(id) ?: throw UserNotFoundException()

    override fun getByUsername(username: String) = repository.findOneByMyUsername(username)

    override fun update(user: User): User = repository.save(user)

    override fun loadUserByUsername(username: String) = repository.findOneByMyUsername(username)

    override fun insert(user: User): User = repository.save(user)

    override fun insert(users: List<User>): List<User> = repository.saveAll(users)

    override fun getAllUsersByLangId(langId: String): List<User> = repository.getUsersByLang(langId)

    override fun getUsersByLangIdCount(langId: String): Long = repository.getUsersByLangCount(langId)

    override fun getUsersByLangWithOffsetAndLimitSortedByScore(langId: String, offset: Int, limit: Int) =
            repository.getUsersByLangWithOffsetAndLimitSortedByScore(langId, offset, limit)

    override fun getLeaderboardUsersByLangWithOffsetAndLimitSortedByScore(langId: String, offset: Int, limit: Int): List<LeaderboardUser> {
        val query = entityManager.createNativeQuery(
                "SELECT " +
                        "u.id, " +
                        "u.full_name as fullName, " +
                        "u.avatar, score, " +
                        "u.level_num as levelNum, " +
                        "u.score_to_next_level as scoreToNextLevel, " +
                        "u.cur_level_score as curLevelScore, " +
                        "COUNT(ra.user_id) as numOfReadArticles " +
                        "FROM users u " +
                        "JOIN users_langs ul ON u.id = ul.user_id " +
                        "LEFT OUTER JOIN read_articles_by_lang ra ON ra.user_id = u.id AND ra.lang_id = :langId " +
                        "WHERE ul.lang_id = :langId " +
                        "GROUP BY u.id " +
                        "ORDER BY u.score DESC OFFSET :offset LIMIT :limit ",
                "LeaderBoardResult"
        )

        query.setParameter("offset", offset)
        query.setParameter("limit", limit)
        query.setParameter("langId", langId)

        val result = query.resultList

        return result as List<LeaderboardUser>
    }

    override fun getByProviderId(id: String, provider: ScpReaderConstants.SocialProvider) = when (provider) {
        ScpReaderConstants.SocialProvider.GOOGLE -> repository.findOneByGoogleId(id)
        ScpReaderConstants.SocialProvider.FACEBOOK -> repository.findOneByFacebookId(id)
        ScpReaderConstants.SocialProvider.VK -> repository.findOneByVkId(id)
    }
}
