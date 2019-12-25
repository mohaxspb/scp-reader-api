package ru.kuchanov.scpreaderapi.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.auth.ClientNotFoundError
import ru.kuchanov.scpreaderapi.repository.auth.ClientDetailsRepository


@Service
class ClientServiceImpl @Autowired constructor(
        val repository: ClientDetailsRepository
) : ClientDetailsService {

    override fun loadClientByClientId(clientId: String): ClientDetails =
            repository.getOne(clientId) ?: throw ClientNotFoundError()
}
