package no.nav.familie.ef.mottak.repository.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SoknadTest {

    @Test
    fun testHashCode() {
        val one = Soknad(15L, "string", Fil(ByteArray(5) { 2 }), "321", "654", "789", false)
        val other = Soknad(15L, "string", Fil(ByteArray(5) { 2 }), "321", "654", "789", false)
        assertEquals(one.hashCode(), other.hashCode())
    }

    @Test
    fun testEquals() {
        val one = Soknad(15L, "string", Fil(ByteArray(5) { 2 }), "321", "654", "789", false)
        val other = Soknad(15L, "string", Fil(ByteArray(5) { 2 }), "321", "654", "789", false)
        assertTrue(one == other)

    }
}