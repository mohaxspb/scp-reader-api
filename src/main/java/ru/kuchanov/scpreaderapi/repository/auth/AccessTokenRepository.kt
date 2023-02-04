package ru.kuchanov.scpreaderapi.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.auth.OAuthAccessToken

@Deprecated("use new auth table")
interface AccessTokenRepository : JpaRepository<OAuthAccessToken, String> {

    fun findFirstByTokenId(tokenId: String): OAuthAccessToken?

    fun findFirstByToken(token: ByteArray): OAuthAccessToken?

    fun findFirstByRefreshToken(token: String): OAuthAccessToken?

    fun findFirstByUserName(tokenId: String): OAuthAccessToken?
}

