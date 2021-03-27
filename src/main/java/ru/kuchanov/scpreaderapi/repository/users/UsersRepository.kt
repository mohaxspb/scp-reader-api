package ru.kuchanov.scpreaderapi.repository.users

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.dto.user.LeaderboardUserProjection
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjection
import ru.kuchanov.scpreaderapi.model.dto.user.UserProjectionV2
import javax.transaction.Transactional

interface UsersRepository : JpaRepository<User, Long> {

    fun findOneByUsername(username: String): User?

    fun findOneByGoogleId(id: String): User?
    fun findOneByFacebookId(id: String): User?
    fun findOneByVkId(id: String): User?
    fun findOneByHuaweiId(id: String): User?

    @Query("""
        select distinct u from User u 
        join UserToHuaweiSubscription uap on u.id = uap.userId 
        where uap.huaweiSubscriptionId = :huaweiSubscriptionId
    """)
    fun getUserByHuaweiSubscriptionId(huaweiSubscriptionId: Long): User?

    @Query(
            "SELECT COUNT(*) from users u JOIN users_langs ul ON u.id = ul.user_id WHERE ul.lang_id = :langId",
            nativeQuery = true
    )
    fun getUsersByLangCount(langId: String): Long

    @Query("select score from User where id = :id")
    fun getScoreById(id: Long): Int

    @Deprecated("Deprecated return type", ReplaceWith("getByIdAsProjectionV2"))
    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    @Query(
            """
                SELECT
                u.id,
                u.full_name as fullName,
                u.avatar,
                u.score,
                u.ads_disabled_end_date as adsDisabledEndDate,
                u.offline_limit_disabled_end_date as offlineLimitDisabledEndDate,
                case 
                    when u.ads_disabled_end_date > timezone('UTC', now()) then true
                    else false
                end as adsDisabled,
                case 
                    when u.offline_limit_disabled_end_date > timezone('UTC', now()) then true
                    else false
                end as offlineLimitDisabled, 
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

    /**
     * It's a full copy of #getByIdAsProjection except of return type.
     */
    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    @Query(
            """
                SELECT
                u.id,
                u.full_name as fullName,
                u.avatar,
                u.score,
                u.ads_disabled_end_date as adsDisabledEndDate,
                u.offline_limit_disabled_end_date as offlineLimitDisabledEndDate,
                case 
                    when u.ads_disabled_end_date > timezone('UTC', now()) then true
                    else false
                end as adsDisabled,
                case 
                    when u.offline_limit_disabled_end_date > timezone('UTC', now()) then true
                    else false
                end as offlineLimitDisabled, 
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
    fun getByIdAsProjectionV2(id: Long): UserProjectionV2?

    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
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

    @Query(
            """
            SELECT count(created) 
            FROM users 
            WHERE created >= CAST( :startDate AS timestamp) 
            AND created <= CAST( :endDate AS timestamp)
        """,
            nativeQuery = true
    )
    fun countUsersCreatedBetweenDates(startDate: String, endDate: String): Int
}
