package ru.kuchanov.scpreaderapi.repository.auth.huawei

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.auth.huawei.HuaweiOAuthAccessToken

interface HuaweiAccessTokenRepository : JpaRepository<HuaweiOAuthAccessToken, Long> {
    fun findFirstByClientId(clientId: String): HuaweiOAuthAccessToken?
}
