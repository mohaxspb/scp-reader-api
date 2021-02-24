package ru.kuchanov.scpreaderapi.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.auth.Authority

interface AuthoritiesRepository : JpaRepository<Authority, String>