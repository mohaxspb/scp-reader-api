package ru.kuchanov.scpreaderapi.utils

import org.springframework.core.io.ClassPathResource
import org.springframework.util.StreamUtils
import java.nio.charset.Charset

object FileUtils {

    fun getFileAsStringFromResources(fileName: String) = StreamUtils.copyToString(
            ClassPathResource(fileName).inputStream,
            Charset.forName("UTF-8")
    )
}