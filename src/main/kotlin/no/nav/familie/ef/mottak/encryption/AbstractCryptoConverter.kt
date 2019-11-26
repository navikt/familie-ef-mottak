package no.nav.familie.ef.mottak.encryption

import org.apache.commons.lang3.StringUtils.isNotEmpty
import java.util.*
import javax.crypto.Cipher
import javax.persistence.AttributeConverter
import kotlin.random.Random


abstract class AbstractCryptoConverter<T>(private val cipherInitializer: CipherInitializer) : AttributeConverter<T, String> {

    abstract fun stringToEntityAttribute(dbData: String): T

    abstract fun entityAttributeToString(attribute: T): String

    override fun convertToDatabaseColumn(attribute: T): String {
        if (isNotEmpty(KeyProperty.DATABASE_ENCRYPTION_KEY) && attribute != null) {
            return encrypt(attribute)
        }
        return entityAttributeToString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): T {
        if (isNotEmpty(KeyProperty.DATABASE_ENCRYPTION_KEY) && isNotEmpty(dbData)) {
            return decrypt(dbData)
        }
        return stringToEntityAttribute(dbData)
    }

    private fun encrypt(attribute: T): String {
        val cipher = cipherInitializer.prepareCipher()
        val bytesToEncrypt = entityAttributeToString(attribute).toByteArray()
        val iv = Random.nextBytes(cipher.blockSize)
        cipherInitializer.initCipher(cipher, Cipher.ENCRYPT_MODE, iv)
        val encryptedBytes = cipher.doFinal(bytesToEncrypt)
        return Base64.getEncoder().encodeToString(cipher.iv + encryptedBytes)
    }

    private fun decrypt(dbData: String): T {
        val cipher = cipherInitializer.prepareCipher()
        val ivAndEncryptedBytes = Base64.getDecoder().decode(dbData)
        val iv = ivAndEncryptedBytes.copyOfRange(0, cipher.blockSize)
        val encryptedBytes = ivAndEncryptedBytes.copyOfRange(cipher.blockSize, ivAndEncryptedBytes.size)
        cipherInitializer.initCipher(cipher, Cipher.DECRYPT_MODE, iv)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return stringToEntityAttribute(String(decryptedBytes))
    }
}
