package ru.kuchanov.scpreaderapi.repository.users

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.users.SiteBaseUrlsToLangs

interface SiteBaseUrlsToLangsRepository : JpaRepository<SiteBaseUrlsToLangs, Long> {
    fun findAllByLangIdOrderById(langId: String): List<SiteBaseUrlsToLangs>
}
