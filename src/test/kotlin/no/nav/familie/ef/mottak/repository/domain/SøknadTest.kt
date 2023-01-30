package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.encryption.EncryptedString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class SøknadTest {

    @Test
    fun testHashCode() {
        val uuid = UUID.randomUUID().toString()
        val opprettet = LocalDateTime.now()
        val one = opprettSøknad(uuid, opprettet)
        val other = opprettSøknad(uuid, opprettet)
        assertEquals(one.hashCode(), other.hashCode())
    }

    @Test
    fun testEquals() {
        val uuid = UUID.randomUUID().toString()
        val opprettet = LocalDateTime.now()
        val one = opprettSøknad(uuid, opprettet)
        val other = opprettSøknad(uuid, opprettet)
        assertTrue(one == other)
    }

    private fun opprettSøknad(
        uuid: String,
        opprettet: LocalDateTime,
    ) =
        Søknad(
            id = uuid,
            søknadJson = EncryptedString("string"),
            søknadPdf = EncryptedFile("321".toByteArray()),
            dokumenttype = "",
            journalpostId = "654",
            saksnummer = "789",
            fnr = "123",
            taskOpprettet = true,
            opprettetTid = opprettet,
        )
}
