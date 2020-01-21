package ru.kuchanov.scpreaderapi.repository.users

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.users.Lang

interface LangsRepository : JpaRepository<Lang, String> {
    fun findOneById(id: String): Lang?
}