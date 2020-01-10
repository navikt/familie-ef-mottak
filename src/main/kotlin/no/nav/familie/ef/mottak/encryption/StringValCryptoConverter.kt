package no.nav.familie.ef.mottak.encryption

class StringValCryptoConverter : AbstractCryptoConverter<String>() {

    override fun byteArrayToEntityAttribute(dbData: ByteArray): String {
        return String(dbData)
    }

    override fun entityAttributeToByteArray(attribute: String): ByteArray {
        return attribute.toByteArray()
    }
}
