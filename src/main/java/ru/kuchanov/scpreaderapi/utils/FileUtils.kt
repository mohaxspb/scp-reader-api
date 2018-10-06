package ru.kuchanov.scpreaderapi.utils

import org.springframework.core.io.ClassPathResource
import org.springframework.util.StreamUtils
import java.io.InputStream
import java.nio.charset.Charset

object FileUtils {

    fun getFileAsStringFromResources(fileName: String): String = StreamUtils.copyToString(
            ClassPathResource(fileName).inputStream,
            Charset.forName("UTF-8")
    )

    fun getFileAsInputStreamFromResources(fileName: String): InputStream = ClassPathResource(fileName).inputStream
}