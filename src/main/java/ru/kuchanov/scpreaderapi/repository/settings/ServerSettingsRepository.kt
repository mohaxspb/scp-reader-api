package ru.kuchanov.scpreaderapi.repository.settings

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.settings.ServerSettings

interface ServerSettingsRepository : JpaRepository<ServerSettings, Long> {

    fun findByKey(key: String): ServerSettings?

    fun findAllByAuthorId(userId: Long): List<ServerSettings>
}
