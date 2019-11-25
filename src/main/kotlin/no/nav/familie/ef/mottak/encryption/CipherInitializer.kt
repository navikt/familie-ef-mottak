package no.nav.familie.ef.mottak.encryption

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CipherInitializer {

    fun prepareCipher(): Cipher {
        return Cipher.getInstance(CIPHER_INSTANCE_NAME)
    }

    internal fun initCipher(cipher: Cipher,
                            encryptionMode: Int,
                            iv: ByteArray) {

        val secretKey = SecretKeySpec(KeyProperty.DATABASE_ENCRYPTION_KEY.toByteArray(), SECRET_KEY_ALGORITHM)
        val algorithmParameters = IvParameterSpec(iv)
        cipher.init(encryptionMode, secretKey, algorithmParameters)
    }

    companion object {
        private const val CIPHER_INSTANCE_NAME = "AES/CBC/PKCS5Padding"
        private const val SECRET_KEY_ALGORITHM = "AES"
    }

}
