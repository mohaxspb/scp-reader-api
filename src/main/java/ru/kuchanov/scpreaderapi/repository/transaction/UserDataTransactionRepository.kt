package ru.kuchanov.scpreaderapi.repository.transaction

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.transaction.UserDataTransaction

interface UserDataTransactionRepository : JpaRepository<UserDataTransaction, String> {
    fun findOneById(id: Long): UserDataTransaction?

    fun findAllByUserId(userId: Long): List<UserDataTransaction>
}