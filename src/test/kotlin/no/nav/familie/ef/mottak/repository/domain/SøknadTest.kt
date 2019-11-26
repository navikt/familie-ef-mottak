package no.nav.familie.ef.mottak.repository.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SøknadTest {

    @Test
    fun testHashCode() {
        val one = Søknad(15L, "string", Pdf(ByteArray(5) { 2 }), "321", "654", "789", false, emptyList())
        val other = Søknad(15L, "string", Pdf(ByteArray(5) { 2 }), "321", "654", "789", false, emptyList())
        assertEquals(one.hashCode(), other.hashCode())
    }

    @Test
    fun testEquals() {
        val one = Søknad(15L, "string", Pdf(ByteArray(5) { 2 }), "321", "654", "789", false, emptyList())
        val other = Søknad(15L, "string", Pdf(ByteArray(5) { 2 }), "321", "654", "789", false, emptyList())
        assertTrue(one == other)

    }
}