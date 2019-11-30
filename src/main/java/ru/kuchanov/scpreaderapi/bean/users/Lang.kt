package ru.kuchanov.scpreaderapi.bean.users

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
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
        // todo change es site from http://lafundacionscp.wikidot.com to http://scp-es.com/
        @Column(name = "site_base_url", columnDefinition = "TEXT")
        var siteBaseUrl: String
)


@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such lang")
class LangNotFoundException : RuntimeException()