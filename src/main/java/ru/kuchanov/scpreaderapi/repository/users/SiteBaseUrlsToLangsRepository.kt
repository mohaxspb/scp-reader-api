package ru.kuchanov.scpreaderapi.repository.users

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.users.SiteBaseUrlsToLangs

interface SiteBaseUrlsToLangsRepository : JpaRepository<SiteBaseUrlsToLangs, Long> {
    fun findAllByLangId(langId: String): List<SiteBaseUrlsToLangs>
}
