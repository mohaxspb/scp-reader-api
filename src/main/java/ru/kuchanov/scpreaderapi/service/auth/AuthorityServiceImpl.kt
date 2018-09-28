package ru.kuchanov.scpreaderapi.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.auth.Authority
import ru.kuchanov.scpreaderapi.repository.auth.AuthoritiesRepository


@Service
class AuthorityServiceImpl : AuthorityService {

    @Autowired
    private lateinit var repository: AuthoritiesRepository

    override fun findAll(): List<Authority> = repository.findAll().toList()

    override fun insert(authority: Authority): Authority = repository.save(authority)

    override fun insert(authorities: List<Authority>): List<Authority> = repository.saveAll(authorities)
}