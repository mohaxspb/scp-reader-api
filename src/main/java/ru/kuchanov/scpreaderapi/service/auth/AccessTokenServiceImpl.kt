package ru.kuchanov.scpreaderapi.service.auth

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ConfigurableObjectInputStream
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.auth.OAuthAccessToken
import ru.kuchanov.scpreaderapi.repository.auth.AccessTokenRepository
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.transaction.Transactional


@Deprecated("Use new auth table")
@Service
class AccessTokenServiceImpl @Autowired constructor(
    private val accessTokenRepository: AccessTokenRepository
) {

    fun findFirstByTokenId(tokenId: String): OAuthAccessToken? {
        return accessTokenRepository.findFirstByTokenId(tokenId)
    }

    fun findFirstByToken(token: String): OAuthAccessToken? {
        return accessTokenRepository.findFirstByToken(serialize(token))
    }

    fun findFirstByRefreshToken(refreshToken: String): OAuthAccessToken? {
        return accessTokenRepository.findFirstByRefreshToken(refreshToken)
    }

    fun findFirstByUserName(userName: String): OAuthAccessToken? {
        return accessTokenRepository.findFirstByUserName(userName)
    }

    private fun serialize(state: Any): ByteArray {
        var oos: ObjectOutputStream? = null
        val var4: ByteArray
        try {
            val bos = ByteArrayOutputStream(512)
            oos = ObjectOutputStream(bos)
            oos.writeObject(state)
            oos.flush()
            var4 = bos.toByteArray()
        } catch (var13: IOException) {
            throw IllegalArgumentException(var13)
        } finally {
            if (oos != null) {
                try {
                    oos.close()
                } catch (var12: IOException) {
                }
            }
        }
        return var4
    }

    fun <T> deserialize(byteArray: ByteArray): T {
        var oip: ObjectInputStream? = null
        val var4: T
        try {
            oip = createObjectInputStream(byteArray)
//            oip = ObjectInputStream(ByteArrayInputStream(byteArray))
            val result: T = oip.readObject() as T
            var4 = result
        } catch (var14: IOException) {
            throw IllegalArgumentException(var14)
        } catch (var15: ClassNotFoundException) {
            throw IllegalArgumentException(var15)
        } finally {
            if (oip != null) {
                try {
                    oip.close()
                } catch (var13: IOException) {
                }
            }
        }
        return var4
    }

    private fun createObjectInputStream(byteArray: ByteArray?): ObjectInputStream {
        return ConfigurableObjectInputStream(ByteArrayInputStream(byteArray), Thread.currentThread().contextClassLoader)
    }

    fun extractTokenKey(value: String?): String? {
        return if (value == null) {
            null
        } else {
            val digest: MessageDigest = try {
                MessageDigest.getInstance("MD5")
            } catch (var5: NoSuchAlgorithmException) {
                throw IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).")
            }
            try {
                val bytes = digest.digest(value.toByteArray(charset("UTF-8")))
                String.format("%032x", BigInteger(1, bytes))
            } catch (var4: UnsupportedEncodingException) {
                throw IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).")
            }
        }
    }
}