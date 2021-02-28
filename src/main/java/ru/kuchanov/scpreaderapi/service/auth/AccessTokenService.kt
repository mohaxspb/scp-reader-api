package ru.kuchanov.scpreaderapi.service.auth

import ru.kuchanov.scpreaderapi.bean.auth.OAuthAccessToken
import javax.transaction.Transactional

interface AccessTokenService {
    fun findAllByClientId(clientId: String): List<OAuthAccessToken>

    @Transactional
    fun delete(oAuthAccessToken: OAuthAccessToken)
}
