package ru.kuchanov.scpreaderapi.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.CrossOrigin
import ru.kuchanov.scpreaderapi.bean.auth.Authority

interface AuthoritiesRepository : JpaRepository<Authority, String>