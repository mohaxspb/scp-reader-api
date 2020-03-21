package ru.kuchanov.scpreaderapi.model.exception

import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ResponseStatus


@ResponseStatus(value = HttpStatus.FORBIDDEN)
class ScpAccessDeniedException(
      override val message: String? = "You don't have permission to do this!"
) : AccessDeniedException(message)