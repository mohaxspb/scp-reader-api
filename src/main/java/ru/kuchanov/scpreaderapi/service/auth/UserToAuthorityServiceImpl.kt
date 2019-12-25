package ru.kuchanov.scpreaderapi.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.auth.UserToAuthority
import ru.kuchanov.scpreaderapi.repository.auth.UsersToAuthoritiesRepository


@Service
class UserToAuthorityServiceImpl @Autowired constructor(
        val repository: UsersToAuthoritiesRepository
) : UserToAuthorityService {

    override fun save(authority: UserToAuthority): UserToAuthority =
            repository.save(authority)

    override fun save(authorities: List<UserToAuthority>): List<UserToAuthority> =
            repository.saveAll(authorities)
}
