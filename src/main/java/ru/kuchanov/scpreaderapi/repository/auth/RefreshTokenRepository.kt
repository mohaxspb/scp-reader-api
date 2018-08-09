package ru.kuchanov.scpreaderapi.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.auth.OAuthRefreshToken

interface RefreshTokenRepository : JpaRepository<OAuthRefreshToken, String>