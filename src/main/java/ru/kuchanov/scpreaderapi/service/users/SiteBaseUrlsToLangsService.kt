package ru.kuchanov.scpreaderapi.service.users

import ru.kuchanov.scpreaderapi.bean.users.SiteBaseUrlsToLangs

interface SiteBaseUrlsToLangsService {
    fun findAllByLangId(langId: String): List<SiteBaseUrlsToLangs>
}
