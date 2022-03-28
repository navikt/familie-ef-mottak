package no.nav.familie.ef.mottak.encryption

data class EncryptedString(val data: String)

class StringValCryptoConverter : AbstractCryptoConverter<EncryptedString?>() {

    override fun byteArrayToEntityAttribute(dbData: ByteArray?): EncryptedString? {
        return dbData?.let { EncryptedString(String(it)) }
    }

    override fun entityAttributeToByteArray(attribute: EncryptedString?): ByteArray? {
        return attribute?.data?.toByteArray()
    }
}
