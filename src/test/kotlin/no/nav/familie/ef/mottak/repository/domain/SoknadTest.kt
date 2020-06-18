package no.nav.familie.ef.mottak.repository.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

internal class SoknadTest {

    @Test
    fun testHashCode() {
        val uuid = UUID.randomUUID().toString()
        val one = Soknad(uuid, "string", Fil("321".toByteArray()), "", "654", "789", "123", true, "vedlegg")
        val other = Soknad(uuid, "string", Fil("321".toByteArray()), "", "654", "789", "123", true, "vedlegg")
        assertEquals(one.hashCode(), other.hashCode())
    }

    @Test
    fun testEquals() {
        val uuid = UUID.randomUUID().toString()
        val one = Soknad(uuid, "string", Fil("321".toByteArray()), "", "654", "789", "123", true, "vedlegg")
        val other = Soknad(uuid, "string", Fil("321".toByteArray()), "", "654", "789", "123", true, "vedlegg")
        assertTrue(one == other)
    }
}