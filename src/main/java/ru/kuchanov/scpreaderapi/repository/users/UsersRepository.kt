package ru.kuchanov.scpreaderapi.repository.users

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.dto.user.LeaderboardUserProjection

interface UsersRepository : JpaRepository<User, Long> {

    fun findOneByMyUsername(username: String): User?

    fun findOneByGoogleId(id: String): User?
    fun findOneByFacebookId(id: String): User?
    fun findOneByVkId(id: String): User?

    @Query("SELECT u from User u JOIN UsersLangs ul ON u.id = ul.userId WHERE ul.langId = :langId")
    fun getUsersByLang(langId: String): List<User>

    @Query(
            "SELECT COUNT(*) from users u JOIN users_langs ul ON u.id = ul.user_id WHERE ul.lang_id = :langId",
            nativeQuery = true
    )
    fun getUsersByLangCount(langId: String): Long

    @Query(
            "SELECT * FROM users u" +
                    " JOIN users_langs ul ON u.id = ul.user_id" +
                    " WHERE ul.lang_id = :langId" +
                    " ORDER BY u.score DESC OFFSET :offset LIMIT :limit",
            nativeQuery = true)
    fun getUsersByLangWithOffsetAndLimitSortedByScore(langId: String, offset: Int, limit: Int): List<User>

    @Query(
            """
                SELECT
                u.id,
                u.full_name as fullName,
                u.avatar,
                u.score,
                u.level_num as levelNum, 
                u.score_to_next_level as scoreToNextLevel, 
                u.cur_level_score as curLevelScore, 
                COUNT(ra.user_id) as numOfReadArticles 
                FROM users u 
                JOIN users_langs ul ON u.id = ul.user_id 
                LEFT OUTER JOIN read__articles_to_lang__to__users ra ON ra.user_id = u.id 
                GROUP BY u.id, u.score  
                ORDER BY u.score DESC OFFSET :offset LIMIT :limit
            """,
            nativeQuery = true
    )
    fun getLeaderboardUsersWithOffsetAndLimitSortedByScore(offset: Int, limit: Int): List<LeaderboardUserProjection>

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
                    where result.id = :userId
                    """,
            nativeQuery = true
    )
    fun getUserPositionInLeaderboard(userId: Long, langId: String): Int
}
