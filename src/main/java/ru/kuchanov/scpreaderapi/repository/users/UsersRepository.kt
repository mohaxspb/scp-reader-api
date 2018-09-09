package ru.kuchanov.scpreaderapi.repository.users

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.users.User

interface UsersRepository : JpaRepository<User, Long> {

    fun findOneByMyUsername(username: String): User?

    @Query("SELECT u from User u JOIN UsersLangs ul ON u.id = ul.userId WHERE ul.langId = :langId")
    fun getUsersByLang(langId: String): List<User>

    @Query("SELECT * FROM users u" +
            " JOIN users_langs ul ON u.id = ul.user_id" +
            " WHERE ul.lang_id = :langId" +
            " ORDER BY u.score DESC OFFSET :offset LIMIT :limit", nativeQuery = true)
    fun getUsersByLangWithOffsetAndLimitSortedByScore(langId: String, offset: Int, limit: Int): List<User>
}