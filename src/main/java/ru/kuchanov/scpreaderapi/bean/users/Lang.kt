package ru.kuchanov.scpreaderapi.bean.users

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import javax.persistence.*

@Entity
@Table(name = "langs")
@NoArgConstructor
data class Lang(
        @Id
        var id: String,
        @Column(name = "lang_code")
        var langCode: String
) {
    @Transient
    var siteBaseUrlsToLangs: List<SiteBaseUrlsToLangs>? = null

    fun removeDomainFromUrl(url: String): String {
        var resultUrl = url
        siteBaseUrlsToLangs?.forEach {
            resultUrl = resultUrl.replace(it.siteBaseUrl, "")
        }
        return resultUrl.trim()
    }
}


@ResponseStatus(value = HttpStatus.NOT_FOUND)
class LangNotFoundException(override val message: String? = "No such lang") : RuntimeException(message)
