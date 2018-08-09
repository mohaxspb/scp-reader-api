package ru.kuchanov.scpreaderapi.service.auth

import ru.kuchanov.scpreaderapi.bean.auth.Authority

interface AuthorityService {
    fun findAll(): List<Authority>
}