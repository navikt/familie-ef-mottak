package no.nav.familie.ef.mottak.repository.domain

data class EncryptedFile(val bytes: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return (bytes.contentEquals((other as EncryptedFile).bytes))
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}
