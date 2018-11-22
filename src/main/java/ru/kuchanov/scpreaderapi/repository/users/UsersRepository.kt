package ru.kuchanov.scpreaderapi.repository.users

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.user.LeaderboardUser

interface UsersRepository : JpaRepository<User, Long> {

    fun findOneByMyUsername(username: String): User?

    fun findOneByGoogleId(id: String): User?
    fun findOneByFacebookId(id: String): User?
    fun findOneByVkId(id: String): User?

    @Query("SELECT u from User u JOIN UsersLangs ul ON u.id = ul.userId WHERE ul.langId = :langId")
    fun getUsersByLang(langId: String): List<User>

    @Query("SELECT COUNT(*) from users u JOIN users_langs ul ON u.id = ul.user_id WHERE ul.lang_id = :langId", nativeQuery = true)
    fun getUsersByLangCount(langId: String): Long

    @Query("SELECT * FROM users u" +
            " JOIN users_langs ul ON u.id = ul.user_id" +
            " WHERE ul.lang_id = :langId" +
            " ORDER BY u.score DESC OFFSET :offset LIMIT :limit", nativeQuery = true)
    fun getUsersByLangWithOffsetAndLimitSortedByScore(langId: String, offset: Int, limit: Int): List<User>

    @Query("SELECT " +
            "id, " +
            "full_name as fullName, " +
            "avatar, score, " +
            "level_num as levelNum, " +
            "score_to_next_level as scoreToNextLevel, " +
            "cur_level_score as curLevelScore " +
            "FROM users u " +
            "JOIN users_langs ul ON u.id = ul.user_id " +
            "WHERE ul.lang_id = :langId " +
            "ORDER BY u.score DESC OFFSET :offset LIMIT :limit", nativeQuery = true)
    fun getLeaderboardUsersByLangWithOffsetAndLimitSortedByScore(langId: String, offset: Int, limit: Int): List<LeaderboardUser>

    /**
     * @see {https://stackoverflow.com/a/3644640/3212712}
     */
    @Query(
            """
                SELECT position
                    FROM (
                       select *,
                            row_number() over(
                               ORDER BY score DESC
                            ) as position
                       FROM users u
                       JOIN users_langs ul ON u.id = ul.user_id
                       WHERE ul.lang_id = :langId
                    ) result
                    where id = :userId
                    """,
            nativeQuery = true
    )
    fun getUserPositionInLeaderboard(userId: Long, langId: String): Int
}
