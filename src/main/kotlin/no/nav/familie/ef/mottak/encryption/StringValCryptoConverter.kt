package no.nav.familie.ef.mottak.encryption

class StringValCryptoConverter(cipherInitializer: CipherInitializer) : AbstractCryptoConverter<String>(cipherInitializer) {

    override fun stringToEntityAttribute(dbData: String): String {
        return dbData
    }

    override fun entityAttributeToString(attribute: String): String {
        return attribute
    }
}
