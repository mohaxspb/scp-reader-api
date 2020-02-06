package ru.kuchanov.scpreaderapi.repository.users

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.dto.user.LeaderboardUserProjection
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjection
import javax.transaction.Transactional

interface UsersRepository : JpaRepository<User, Long> {

    fun findOneByUsername(username: String): User?

    fun findOneByGoogleId(id: String): User?
    fun findOneByFacebookId(id: String): User?
    fun findOneByVkId(id: String): User?

    @Query(
            "SELECT COUNT(*) from users u JOIN users_langs ul ON u.id = ul.user_id WHERE ul.lang_id = :langId",
            nativeQuery = true
    )
    fun getUsersByLangCount(langId: String): Long

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
                COALESCE(read_count.count, 0) as numOfReadArticles 
                FROM users u 
                LEFT OUTER JOIN (
                    select ra.user_id as user_id, count(distinct al.article_id) as count
                    from read__articles_to_lang__to__users ra
                    join articles_langs al on ra.article_to_lang_id = al.id
                    group by ra.user_id
                ) read_count on read_count.user_id = u.id
                WHERE u.id = :id 
                GROUP BY u.id, u.score, read_count.count
            """,
            nativeQuery = true
    )
    fun getByIdAsProjection(id: Long): UserProjection?

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
                COALESCE(read_count.count, 0) as numOfReadArticles 
                FROM users u 
                LEFT OUTER JOIN (
                    select ra.user_id as user_id, count(distinct al.article_id) as count
                    from read__articles_to_lang__to__users ra
                    join articles_langs al on ra.article_to_lang_id = al.id
                    group by ra.user_id
                ) read_count on read_count.user_id = u.id
                GROUP BY u.id, u.score, read_count.count
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

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(
            """
                update users 
                set full_name = :name, avatar = :avatarUrl
                where id = :userId
                """,
            nativeQuery = true
    )
    fun editAccount(userId: Long, name: String, avatarUrl: String)
}
