package ru.kuchanov.scpreaderapi.utils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import javax.persistence.AttributeConverter
import javax.persistence.Converter


/**
 * Encodes data in DB and do not decodes it. Use it for storing passwords
 */
@Component
@Converter
@Configurable
class EncryptionConverter : AttributeConverter<String, String> {

    companion object {
        lateinit var passwordEncoder: PasswordEncoder
    }

    /**
     * we need to use static field as we cant inject in simple property
     */
    @Autowired
    fun init(passwordEncoder: PasswordEncoder) {
        EncryptionConverter.passwordEncoder = passwordEncoder
    }

    override fun convertToDatabaseColumn(attribute: String?): String = passwordEncoder.encode(attribute)

    override fun convertToEntityAttribute(dbData: String?) = dbData
}