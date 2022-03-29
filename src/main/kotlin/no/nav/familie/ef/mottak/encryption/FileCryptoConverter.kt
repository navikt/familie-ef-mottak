package no.nav.familie.ef.mottak.encryption

import no.nav.familie.ef.mottak.repository.domain.EncryptedFile


class FileCryptoConverter : AbstractCryptoConverter<EncryptedFile?>() {

    override fun byteArrayToEntityAttribute(dbData: ByteArray?): EncryptedFile? {
        return dbData?.let { EncryptedFile(it) }
    }

    override fun entityAttributeToByteArray(attribute: EncryptedFile?): ByteArray? {
        return attribute?.bytes
    }
}
