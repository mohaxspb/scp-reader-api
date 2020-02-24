package ru.kuchanov.scpreaderapi.repository.transaction

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.transaction.UserDataTransaction

interface UserDataTransactionRepository : JpaRepository<UserDataTransaction, String> {

    fun findByTransactionTypeAndArticleToLangIdAndUserId(
            transactionType: ScpReaderConstants.UserDataTransactionType,
            articleToLangId: Long,
            userId: Long
    ): UserDataTransaction?

    fun findAllByUserId(userId: Long): List<UserDataTransaction>
}
