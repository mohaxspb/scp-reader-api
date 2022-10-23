package ru.kuchanov.scpreaderapi.service.auth

import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService

interface ScpOAuth2AuthorizationService : OAuth2AuthorizationService {

    fun generateAndSaveToken(email: String?, clientId: String): Map<String, Any>
}