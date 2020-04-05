package ru.kuchanov.scpreaderapi.model.dto.monetization

import ru.kuchanov.scpreaderapi.bean.transaction.UserDataTransaction


data class AddUserDataTransactionResultDto(
        val userDataTransaction: UserDataTransaction,
        val totalUserScore: Int
)