package ru.kuchanov.scpreaderapi.model.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus


@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class ScpServerException(
        override val message: String? = "Unexpected error!",
        override val cause: Throwable? = null
) : RuntimeException(message, cause)