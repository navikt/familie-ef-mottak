package no.nav.familie.ef.mottak.encryption

import javax.crypto.Cipher
import javax.persistence.AttributeConverter
import kotlin.random.Random


abstract class AbstractCryptoConverter<T> : AttributeConverter<T, ByteArray> {

    private val cipherInitializer = CipherInitializer()

    abstract fun byteArrayToEntityAttribute(dbData: ByteArray): T

    abstract fun entityAttributeToByteArray(attribute: T): ByteArray

    override fun convertToDatabaseColumn(attribute: T): ByteArray {
        if (KeyProperty.DATABASE_ENCRYPTION_KEY.isNotEmpty() && attribute != null) {
            return encrypt(attribute)
        }
        return entityAttributeToByteArray(attribute)
    }

    override fun convertToEntityAttribute(dbData: ByteArray): T {
        if (KeyProperty.DATABASE_ENCRYPTION_KEY.isNotEmpty() && dbData.isNotEmpty()) {
            return decrypt(dbData)
        }
        return byteArrayToEntityAttribute(dbData)
    }

    private fun encrypt(attribute: T): ByteArray {
        val cipher = cipherInitializer.prepareCipher()
        val bytesToEncrypt = entityAttributeToByteArray(attribute)
        val initializationVector = Random.nextBytes(cipher.blockSize)
        cipherInitializer.initCipher(cipher, Cipher.ENCRYPT_MODE, initializationVector)
        val encryptedBytes = cipher.doFinal(bytesToEncrypt)
        return cipher.iv + encryptedBytes
    }

    private fun decrypt(ivAndEncryptedBytes: ByteArray): T {
        val cipher = cipherInitializer.prepareCipher()
        val initializationVector = ivAndEncryptedBytes.copyOfRange(0, cipher.blockSize)
        val encryptedBytes = ivAndEncryptedBytes.copyOfRange(cipher.blockSize, ivAndEncryptedBytes.size)
        cipherInitializer.initCipher(cipher, Cipher.DECRYPT_MODE, initializationVector)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return byteArrayToEntityAttribute(decryptedBytes)
    }
}
