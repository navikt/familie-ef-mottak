package no.nav.familie.ef.mottak.encryption

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KeyProperty {

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(@Value("\${example.database.encryption.key}") databaseEncryptionKey: String) {
        DATABASE_ENCRYPTION_KEY = databaseEncryptionKey.toByteArray()
    }

    companion object {
        var DATABASE_ENCRYPTION_KEY: ByteArray = ByteArray(0)
    }
}
