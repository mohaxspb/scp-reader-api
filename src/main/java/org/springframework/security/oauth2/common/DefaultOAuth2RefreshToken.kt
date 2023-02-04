package org.springframework.security.oauth2.common

import java.io.Serializable

@Deprecated("We need it to read token data from byte array in application")
data class DefaultOAuth2RefreshToken(
    val value: String
) : Serializable {
    companion object {
        const val serialVersionUID = 8349970621900575838L
    }
}