package ru.kuchanov.scpreaderapi.service.transaction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.transaction.UserDataTransaction
import ru.kuchanov.scpreaderapi.repository.transaction.UserDataTransactionRepository
import ru.kuchanov.scpreaderapi.repository.transaction.UserDataTransactionService

@Service
class UserDataTransactionServiceImpl @Autowired constructor(
        val repository: UserDataTransactionRepository
) : UserDataTransactionService {

    override fun findOneById(id: Long): UserDataTransaction? =
            repository.findOneById(id)

    override fun findAllByUserId(userId: Long): List<UserDataTransaction> =
            repository.findAllByUserId(userId)

    override fun save(transaction: UserDataTransaction): UserDataTransaction =
            repository.save(transaction)
}