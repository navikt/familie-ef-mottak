package no.nav.familie.ef.mottak.encryption

import org.springframework.stereotype.Component
import java.security.SecureRandom
import javax.crypto.Cipher

@Component
class CryptoService {
    private val cipherInitializer = CipherInitializer()

    fun encrypt(s: String): ByteArray {
        val plainBytes = s.toByteArray()
        val cipher = cipherInitializer.prepareCipher()
        val iv = ByteArray(cipher.blockSize)
        SecureRandom().nextBytes(iv)

        cipherInitializer.initCipher(cipher, Cipher.ENCRYPT_MODE, iv)
        val encryptedBytes = cipher.doFinal(plainBytes)

        return iv + encryptedBytes
    }

    fun decrypt(ivAndEncryptedBytes: ByteArray): ByteArray {
        val cipher = cipherInitializer.prepareCipher()
        val iv = ivAndEncryptedBytes.copyOfRange(0, cipher.blockSize)
        val encryptedBytes =
            ivAndEncryptedBytes.copyOfRange(cipher.blockSize, ivAndEncryptedBytes.size)

        cipherInitializer.initCipher(cipher, Cipher.DECRYPT_MODE, iv)
        return cipher.doFinal(encryptedBytes)
    }
}
