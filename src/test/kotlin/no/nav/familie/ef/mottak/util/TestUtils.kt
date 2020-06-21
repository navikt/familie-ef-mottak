package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util

object TestUtils {
    fun readFile(filnavn: String): String {
        return this::class.java.getResource("/json/$filnavn").readText()
    }
}