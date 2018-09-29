package ru.kuchanov.scpreaderapi.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.stereotype.Service

@Service
class AccessTokenServices : ResourceServerTokenServices {

    @Autowired
    lateinit var tokenStore: TokenStore

    override fun loadAuthentication(accessToken: String): OAuth2Authentication = tokenStore.readAuthentication(accessToken)

    override fun readAccessToken(accessToken: String): OAuth2AccessToken = tokenStore.readAccessToken(accessToken)
}