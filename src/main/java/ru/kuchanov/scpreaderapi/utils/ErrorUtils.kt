package ru.kuchanov.scpreaderapi.utils

import org.springframework.stereotype.Service
import java.io.PrintWriter
import java.io.StringWriter

@Service
class ErrorUtils {

    fun stackTraceAsString(e: Throwable?): String? {
        val stringWriter = StringWriter()
        e?.printStackTrace(PrintWriter(stringWriter))
        return e?.let { stringWriter.toString() }
    }
}