package no.nav.familie.ef.mottak.encryption

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KeyProperty {
    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(
        @Value("\${database.encryption.key}") dbEncryptionKey: String,
    ) {
        databaseEncryptionKey = dbEncryptionKey.toByteArray()
    }

    companion object {
        var databaseEncryptionKey: ByteArray = ByteArray(0)
    }
}
