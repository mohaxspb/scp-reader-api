package ru.kuchanov.scpreaderapi.repository.transaction

import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.transaction.UserDataTransaction

interface UserDataTransactionService {

    fun findByTransactionTypeAndArticleToLangIdAndUserId(
            transactionType: ScpReaderConstants.UserDataTransactionType,
            articleToLangId: Long,
            userId: Long
    ): UserDataTransaction?

    fun findAllByUserId(userId: Long): List<UserDataTransaction>

    fun save(transaction: UserDataTransaction): UserDataTransaction
}