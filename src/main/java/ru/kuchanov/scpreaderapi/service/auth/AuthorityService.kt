package ru.kuchanov.scpreaderapi.service.auth

import ru.kuchanov.scpreaderapi.bean.auth.Authority

interface AuthorityService {
    fun findAll(): List<Authority>
    fun insert(authority: Authority): Authority?
    fun insert(authorities: List<Authority>): List<Authority>
}