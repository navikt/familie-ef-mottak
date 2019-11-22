package no.nav.familie.ef.mottak.encryption

import no.nav.familie.ef.mottak.encryption.KeyProperty.Companion.DATABASE_ENCRYPTION_KEY
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KeyProperty {

    constructor(@Value("\${example.database.encryption.key}") databaseEncryptionKey: String) {
        DATABASE_ENCRYPTION_KEY = databaseEncryptionKey
    }

    companion object {
        lateinit var DATABASE_ENCRYPTION_KEY: String
    }
}
