package ru.kuchanov.scpreaderapi.bean.users

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "langs")
data class Lang(
        @Id
        var id: String,
        @Column(name = "lang_code")
        var langCode: String,
        @Column(name = "site_base_url")
        var siteBaseUrl: String
)