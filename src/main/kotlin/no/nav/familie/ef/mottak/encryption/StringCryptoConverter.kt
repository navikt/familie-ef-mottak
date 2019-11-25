package no.nav.familie.ef.mottak.encryption

import no.nav.familie.ef.mottak.encryption.KeyProperty.Companion.DATABASE_ENCRYPTION_KEY
import org.apache.commons.lang3.StringUtils.isNotEmpty
import java.util.*
import javax.crypto.Cipher
import javax.persistence.AttributeConverter
import kotlin.random.Random

class StringCryptoConverter(private val cipherInitializer: CipherInitializer) : AttributeConverter<String, String> {

    override fun convertToDatabaseColumn(attribute: String?): String? {
        if (isNotEmpty(DATABASE_ENCRYPTION_KEY) && isNotEmpty(attribute)) {
            return encrypt(attribute!!)
        }
        return attribute
    }

    override fun convertToEntityAttribute(dbData: String?): String? {
        if (isNotEmpty(DATABASE_ENCRYPTION_KEY) && isNotEmpty(dbData)) {
            return decrypt(dbData!!)
        }
        return dbData
    }

    private fun encrypt(attribute: String): String {
        val cipher = cipherInitializer.prepareCipher()
        val bytesToEncrypt = attribute.toByteArray()
        val iv = Random.nextBytes(cipher.blockSize)
        cipherInitializer.initCipher(cipher, Cipher.ENCRYPT_MODE, iv)
        val encryptedBytes = cipher.doFinal(bytesToEncrypt)
        return Base64.getEncoder().encodeToString(cipher.iv + encryptedBytes)
    }

    private fun decrypt(dbData: String): String {
        val cipher = cipherInitializer.prepareCipher()
        val ivAndEncryptedBytes = Base64.getDecoder().decode(dbData)
        val iv = ivAndEncryptedBytes.copyOfRange(0, cipher.blockSize)
        val encryptedBytes = ivAndEncryptedBytes.copyOfRange(cipher.blockSize, ivAndEncryptedBytes.size)
        cipherInitializer.initCipher(cipher, Cipher.DECRYPT_MODE, iv)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
}
