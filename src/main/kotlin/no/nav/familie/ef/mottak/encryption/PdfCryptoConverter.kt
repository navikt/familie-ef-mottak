package no.nav.familie.ef.mottak.encryption

import no.nav.familie.ef.mottak.repository.domain.Pdf
import java.util.*


class PdfCryptoConverter(cipherInitializer: CipherInitializer) : AbstractCryptoConverter<Pdf>(cipherInitializer) {

    override fun stringToEntityAttribute(dbData: String): Pdf {
        return Pdf(dbData.toByteArray())
    }

    override fun entityAttributeToString(attribute: Pdf): String {
        return Base64.getEncoder().encodeToString(attribute.bytes)
    }

}
