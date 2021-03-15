package ru.kuchanov.scpreaderapi.repository.push

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.push.UsersToPushTokens

interface UsersToPushTokenRepository : JpaRepository<UsersToPushTokens, Long> {

    fun findAllByUserId(userId: Long): List<UsersToPushTokens>
}
