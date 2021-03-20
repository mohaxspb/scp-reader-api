package ru.kuchanov.scpreaderapi.service.push

import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.push.UsersToPushTokens

interface UserToPushTokensService {

    fun save(usersToPushTokens: UsersToPushTokens): UsersToPushTokens

    fun findByUserIdAndPushTokenProviderAndPushTokenValue(
            userId: Long,
            pushTokenProvider: ScpReaderConstants.Push.Provider,
            pushTokenValue: String
    ): UsersToPushTokens?

    fun findByUserIdAndPushTokenValue(
            userId: Long,
            pushTokenValue: String
    ): UsersToPushTokens?

    fun deleteById(id: Long)

    fun findAllByUserId(userId: Long): List<UsersToPushTokens>
}