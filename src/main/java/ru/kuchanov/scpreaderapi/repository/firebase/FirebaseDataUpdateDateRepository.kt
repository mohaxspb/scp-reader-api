package ru.kuchanov.scpreaderapi.repository.firebase

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.FirebaseDataUpdateDate

interface FirebaseDataUpdateDateRepository : JpaRepository<FirebaseDataUpdateDate, Long> {
    fun findOneByLangId(langId: String): FirebaseDataUpdateDate?
}