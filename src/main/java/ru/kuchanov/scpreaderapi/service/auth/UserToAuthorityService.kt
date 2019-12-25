package ru.kuchanov.scpreaderapi.service.auth

import ru.kuchanov.scpreaderapi.bean.auth.UserToAuthority

interface UserToAuthorityService {
    fun save(authority: UserToAuthority): UserToAuthority
    fun save(authorities: List<UserToAuthority>): List<UserToAuthority>
}
