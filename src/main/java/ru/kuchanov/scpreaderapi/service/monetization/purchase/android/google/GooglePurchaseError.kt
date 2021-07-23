package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class GooglePurchaseError(
    override val message: String?,
    override val cause: Throwable?
) : RuntimeException(message, cause)