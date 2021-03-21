package ru.kuchanov.scpreaderapi.service.push

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.push.UsersToPushTokens
import ru.kuchanov.scpreaderapi.repository.push.UsersToPushTokenRepository

@Service
class UserToPushTokensServiceImpl @Autowired constructor(
        private val repository: UsersToPushTokenRepository
) : UserToPushTokensService {

    override fun save(usersToPushTokens: UsersToPushTokens): UsersToPushTokens =
            repository.save(usersToPushTokens)

    override fun findByUserIdAndPushTokenProviderAndPushTokenValue(
            userId: Long,
            pushTokenProvider: ScpReaderConstants.Push.Provider,
            pushTokenValue: String
    ): UsersToPushTokens? =
            repository.findByUserIdAndPushTokenProviderAndPushTokenValue(userId, pushTokenProvider, pushTokenValue)

    override fun findByUserIdAndPushTokenValue(
            userId: Long,
            pushTokenValue: String
    ): UsersToPushTokens? =
            repository.findByUserIdAndPushTokenValue(userId, pushTokenValue)

    override fun findByUserIdAndPushTokenProvider(
            userId: Long,
            pushTokenProvider: ScpReaderConstants.Push.Provider
    ): List<UsersToPushTokens> =
            repository.findByUserIdAndPushTokenProvider(userId, pushTokenProvider)

    override fun findAllByUserId(userId: Long): List<UsersToPushTokens> =
            repository.findAllByUserId(userId)

    override fun deleteById(id: Long) =
            repository.deleteById(id)

    override fun deleteByPushTokenValue(pushTokenValue: String) =
            repository.deleteByPushTokenValue(pushTokenValue)
}