package ru.kuchanov.scpreaderapi.service.users

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.repository.users.LangsRepository


@Service
class LangServiceImpl @Autowired constructor(
        val repository: LangsRepository,
        val siteBaseUrlsToLangsService: SiteBaseUrlsToLangsService
) : LangService {

    override fun findAll(): List<Lang> = repository.findAll().map { it.withSiteBaseUrls() }

    override fun getById(id: String) = repository.findOneById(id)?.withSiteBaseUrls() ?: throw LangNotFoundException()

    fun Lang.withSiteBaseUrls() =
            this.apply { siteBaseUrlsToLangs = siteBaseUrlsToLangsService.findAllByLangId(id) }
}
