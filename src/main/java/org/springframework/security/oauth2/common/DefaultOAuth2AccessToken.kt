package org.springframework.security.oauth2.common

import java.io.Serializable
import java.util.*

@Deprecated("We need it to read token data from byte array in application")
data class DefaultOAuth2AccessToken(
    val value: String,
    val expiration: Date,
    val tokenType: String,
    val refreshToken: DefaultOAuth2RefreshToken,
    val scope: Set<String>,
    val additionalInformation: Map<String, Any?>
) : Serializable {
    companion object {
        const val serialVersionUID = 914967629530462926L
    }
}