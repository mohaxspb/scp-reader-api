package ru.kuchanov.scpreaderapi.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.TokenRequest
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.stereotype.Service

//@Service
//class AccessTokenServices2 : DefaultTokenServices {
//
//    @Autowired
//    lateinit var tokenStore: TokenStore
//
//    override fun getAccessToken(authentication: OAuth2Authentication?): OAuth2AccessToken = tokenStore.getAccessToken(authentication)
//
//    override fun createAccessToken(authentication: OAuth2Authentication?): OAuth2AccessToken =
//            tokenStore.storeAccessToken(authentication)
//
//    override fun refreshAccessToken(refreshToken: String?, tokenRequest: TokenRequest?): OAuth2AccessToken {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    //    override fun loadAuthentication(accessToken: String): OAuth2Authentication = tokenStore.readAuthentication(accessToken)
////
////    override fun readAccessToken(accessToken: String): OAuth2AccessToken = tokenStore.readAccessToken(accessToken)
//}