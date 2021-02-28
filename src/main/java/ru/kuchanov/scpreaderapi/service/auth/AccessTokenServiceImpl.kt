package ru.kuchanov.scpreaderapi.service.auth

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.auth.OAuthAccessToken
import ru.kuchanov.scpreaderapi.repository.auth.AccessTokenRepository
import javax.transaction.Transactional


@Service
class AccessTokenServiceImpl @Autowired constructor(
        val repository: AccessTokenRepository,
        val log: Logger
) : AccessTokenService {

    override fun findAllByClientId(clientId: String): List<OAuthAccessToken> =
            repository.findAllByClientId(clientId)

    @Transactional
    override fun delete(oAuthAccessToken: OAuthAccessToken) {
        log.error("Delete redundant oAuthAccessToken: $oAuthAccessToken")
        repository.delete(oAuthAccessToken)
    }
}
