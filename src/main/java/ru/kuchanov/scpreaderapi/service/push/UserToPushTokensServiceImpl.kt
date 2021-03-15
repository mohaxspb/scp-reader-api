package ru.kuchanov.scpreaderapi.service.push

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.push.UsersToPushTokens
import ru.kuchanov.scpreaderapi.repository.push.UsersToPushTokenRepository

@Service
class UserToPushTokensServiceImpl @Autowired constructor(
        private val repository: UsersToPushTokenRepository
) : UserToPushTokensService {

    override fun save(usersToPushTokens: UsersToPushTokens): UsersToPushTokens =
            repository.save(usersToPushTokens)

    override fun findAllByUserId(userId: Long): List<UsersToPushTokens> =
            repository.findAllByUserId(userId)
}