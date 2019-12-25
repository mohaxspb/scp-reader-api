package ru.kuchanov.scpreaderapi.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.auth.UserToAuthority

interface UsersToAuthoritiesRepository : JpaRepository<UserToAuthority, Long>