package ru.kuchanov.scpreaderapi.service.push

import ru.kuchanov.scpreaderapi.bean.push.UsersToPushTokens

interface UserToPushTokensService {

    fun save(usersToPushTokens: UsersToPushTokens): UsersToPushTokens

    fun findAllByUserId(userId: Long): List<UsersToPushTokens>
}