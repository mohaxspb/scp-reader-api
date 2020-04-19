package ru.kuchanov.scpreaderapi.service.settings

import ru.kuchanov.scpreaderapi.bean.settings.ServerSettings


interface ServerSettingsService {

    fun findByKey(key: String): ServerSettings?

    fun findAllByAuthorId(userId: Long): List<ServerSettings>

    fun save(serverSettings: ServerSettings): ServerSettings
}