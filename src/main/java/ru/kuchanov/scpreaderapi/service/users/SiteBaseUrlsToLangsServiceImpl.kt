package ru.kuchanov.scpreaderapi.service.users

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.users.SiteBaseUrlsToLangs
import ru.kuchanov.scpreaderapi.repository.users.SiteBaseUrlsToLangsRepository


@Service
class SiteBaseUrlsToLangsServiceImpl @Autowired constructor(
        private val siteBaseUrlsToLangsRepository: SiteBaseUrlsToLangsRepository
) : SiteBaseUrlsToLangsService {

    override fun findAllByLangId(langId: String): List<SiteBaseUrlsToLangs> =
            siteBaseUrlsToLangsRepository.findAllByLangId(langId)
}
