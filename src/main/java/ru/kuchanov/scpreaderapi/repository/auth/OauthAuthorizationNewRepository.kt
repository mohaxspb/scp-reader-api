package ru.kuchanov.scpreaderapi.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.auth.OAuthAuthorizationNew

interface OauthAuthorizationNewRepository : JpaRepository<OAuthAuthorizationNew, Long> {

    fun findFirstByAccessTokenValue(accessToken: String): OAuthAuthorizationNew?

    fun findFirstByRefreshTokenValue(token: String): OAuthAuthorizationNew?

    fun findFirstByPrincipalName(principalName: String): OAuthAuthorizationNew?

    fun findFirstByRegisteredClientId(registeredClientId: String): OAuthAuthorizationNew?
}

