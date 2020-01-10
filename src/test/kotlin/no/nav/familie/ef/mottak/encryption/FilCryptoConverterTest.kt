package no.nav.familie.ef.mottak.encryption

import no.nav.familie.ef.mottak.repository.domain.Fil
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

internal class FilCryptoConverterTest {

    private val fileCryptoConverter: FileCryptoConverter

    private val fil = Fil(ByteArray(50) { i -> (i * i).toByte() })

    init {
        KeyProperty("kdjeuyfjkekhndlknvfdekljnolrhsdo")
        fileCryptoConverter = FileCryptoConverter()
    }


    @Test
    internal fun `convertToDatabaseColumn konverterer input til String for lagring i base og tilbake til identisk string`() {
        val convertToDatabaseColumn = fileCryptoConverter.convertToDatabaseColumn(fil)

        Assertions.assertThat(convertToDatabaseColumn).isNotEqualTo(fil)
    }

    @Test
    internal fun `convertToDatabaseColumn konverterer input til String for lagring i base`() {
        // Laget ved å kjøre base64 encoding på resultatet fra kryptering av fil.
        val encrypted = "wIFKKv38vySQRm7Y9Pusv2lzEy9I9/IyM4/UkA2e7RiziKK2oVz1K/N" +
                        "uU8DyXHh2vZsWvkCz7Bx0VCMwbO0VEUt4shyTDxYhuB5OdV0Utrk="

        val convertToEntityAttribute = fileCryptoConverter.convertToEntityAttribute(Base64.getDecoder().decode(encrypted))

        Assertions.assertThat(convertToEntityAttribute).isEqualTo(fil)

    }


}