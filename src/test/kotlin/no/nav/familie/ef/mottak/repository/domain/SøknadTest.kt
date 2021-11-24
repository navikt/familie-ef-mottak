package no.nav.familie.ef.mottak.repository.domain

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
        val one = Søknad(uuid, "string", Fil("321".toByteArray()), "", "654", "789", "123", true, opprettet)
        val other = Søknad(uuid, "string", Fil("321".toByteArray()), "", "654", "789", "123", true, opprettet)
        assertEquals(one.hashCode(), other.hashCode())
    }

    @Test
    fun testEquals() {
        val uuid = UUID.randomUUID().toString()
        val opprettet = LocalDateTime.now()
        val one = Søknad(uuid, "string", Fil("321".toByteArray()), "", "654", "789", "123", true, opprettet)
        val other = Søknad(uuid, "string", Fil("321".toByteArray()), "", "654", "789", "123", true, opprettet)
        assertTrue(one == other)
    }
}