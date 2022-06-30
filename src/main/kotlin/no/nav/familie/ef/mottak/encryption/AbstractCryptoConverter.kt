package no.nav.familie.ef.mottak.encryption

import org.springframework.core.convert.converter.Converter
import javax.crypto.Cipher
import kotlin.random.Random

private val cipherInitializer = CipherInitializer()

abstract class AbstractCryptoReadingConverter<T> : Converter<ByteArray, T> {

    abstract fun byteArrayToEntityAttribute(dbData: ByteArray?): T

    override fun convert(dbData: ByteArray): T? {
        if (KeyProperty.DATABASE_ENCRYPTION_KEY.isNotEmpty() && dbData.isNotEmpty()) {
            return decrypt(dbData)
        }
        return byteArrayToEntityAttribute(dbData)
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

abstract class AbstractCryptoWritingConverter<T> : Converter<T, ByteArray> {

    abstract fun entityAttributeToByteArray(attribute: T): ByteArray?

    override fun convert(attribute: T): ByteArray? {
        if (KeyProperty.DATABASE_ENCRYPTION_KEY.isNotEmpty() && attribute != null) {
            return encrypt(attribute)
        }
        return entityAttributeToByteArray(attribute)
    }

    private fun encrypt(attribute: T): ByteArray {
        val cipher = cipherInitializer.prepareCipher()
        val bytesToEncrypt = entityAttributeToByteArray(attribute)
        val initializationVector = Random.nextBytes(cipher.blockSize)
        cipherInitializer.initCipher(cipher, Cipher.ENCRYPT_MODE, initializationVector)
        val encryptedBytes = cipher.doFinal(bytesToEncrypt)
        return cipher.iv + encryptedBytes
    }
}
