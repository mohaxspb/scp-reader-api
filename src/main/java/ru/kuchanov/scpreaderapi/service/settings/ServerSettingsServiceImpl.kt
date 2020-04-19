package ru.kuchanov.scpreaderapi.service.settings

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.settings.ServerSettings
import ru.kuchanov.scpreaderapi.repository.settings.ServerSettingsRepository

@Service
class ServerSettingsServiceImpl @Autowired constructor(
        val repository: ServerSettingsRepository
) : ServerSettingsService {

    override fun findByKey(key: String): ServerSettings? =
            repository.findByKey(key)

    override fun findAllByAuthorId(userId: Long): List<ServerSettings> =
            repository.findAllByAuthorId(userId)

    override fun save(serverSettings: ServerSettings): ServerSettings =
            repository.save(serverSettings)
}