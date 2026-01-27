package no.nav.familie.ef.mottak.encryption

import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

data class EncryptedString(
    val data: String,
)

@WritingConverter
class StringValCryptoWritingConverter : AbstractCryptoWritingConverter<EncryptedString>() {
    override fun entityAttributeToByteArray(attribute: EncryptedString): ByteArray = attribute.data.toByteArray()
}

@ReadingConverter
class StringValCryptoReadingConverter : AbstractCryptoReadingConverter<EncryptedString?>() {
    override fun byteArrayToEntityAttribute(dbData: ByteArray?): EncryptedString? = dbData?.let { EncryptedString(String(it)) }
}
