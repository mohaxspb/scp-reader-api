package ru.kuchanov.scpreaderapi.repository.transaction

import ru.kuchanov.scpreaderapi.bean.transaction.UserDataTransaction

interface UserDataTransactionService {
    fun findOneById(id: Long): UserDataTransaction?

    fun findAllByUserId(userId: Long): List<UserDataTransaction>

    fun save(transaction: UserDataTransaction): UserDataTransaction
}