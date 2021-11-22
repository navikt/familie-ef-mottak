package no.nav.familie.ef.mottak.encryption

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Base64

internal class StringValCryptoConverterTest {

    private val stringValCryptoConverter: StringValCryptoConverter

    init {
        KeyProperty("kdjeuyfjkekhndlknvfdekljnolrhsdo")
        stringValCryptoConverter = StringValCryptoConverter()
    }


    @Test
    internal fun `convertToDatabaseColumn konverterer input til String for lagring i base og tilbake til identisk string`() {
        val string = "Bob Marley"

        val convertToDatabaseColumn = stringValCryptoConverter.convertToDatabaseColumn(string)

        assertThat(convertToDatabaseColumn).isNotEqualTo("Bob Marley")
    }

    @Test
    internal fun `convertToDatabaseColumn konverterer input til String for lagring i base`() {
        val string = "UQO9DFBIipAyzXzGIG6XmGT3fOBDdIFA6AvpH3Adpow="

        val toDatabaseColumn = stringValCryptoConverter.convertToEntityAttribute(Base64.getDecoder().decode(string))

        assertThat(toDatabaseColumn).isEqualTo("Bob Marley")
    }

    @Test
    fun byteArrayToEntityAttribute() {
        val byteArray = byteArrayOf('b'.toByte(), 'o'.toByte(), 'b'.toByte())

        val byteArrayToEntityAttribute = stringValCryptoConverter.byteArrayToEntityAttribute(byteArray)

        assertThat(byteArrayToEntityAttribute).isEqualTo("bob")
    }

    @Test
    fun entityAttributeToByteArray() {
        val byteArray = byteArrayOf('b'.toByte(), 'o'.toByte(), 'b'.toByte())

        val entityAttributeToByteArray = stringValCryptoConverter.entityAttributeToByteArray("bob")

        assertThat(entityAttributeToByteArray).isEqualTo(byteArray)
    }
}