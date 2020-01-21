package ru.kuchanov.scpreaderapi.bean.users

import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "site_base_urls__to__langs")
@NoArgConstructor
data class SiteBaseUrlsToLangs(
        @Id
        val id: Long,
        @Column(name = "lang_id")
        val langId: String,
        @Column(name = "site_base_url")
        var siteBaseUrl: String
)
