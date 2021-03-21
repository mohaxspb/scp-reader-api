package ru.kuchanov.scpreaderapi.repository.push

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.push.UsersToPushTokens
import javax.transaction.Transactional

interface UsersToPushTokenRepository : JpaRepository<UsersToPushTokens, Long> {

    fun findByUserIdAndPushTokenProviderAndPushTokenValue(
            userId: Long,
            pushTokenProvider: ScpReaderConstants.Push.Provider,
            pushTokenValue: String
    ): UsersToPushTokens?

    fun findByUserIdAndPushTokenValue(
            userId: Long,
            pushTokenValue: String
    ): UsersToPushTokens?

    fun findByUserIdAndPushTokenProvider(
            userId: Long,
            pushTokenProvider: ScpReaderConstants.Push.Provider
    ): List<UsersToPushTokens>

    fun findAllByUserId(userId: Long): List<UsersToPushTokens>

    @Transactional
    fun deleteByPushTokenValue(pushTokenValue: String)
}
