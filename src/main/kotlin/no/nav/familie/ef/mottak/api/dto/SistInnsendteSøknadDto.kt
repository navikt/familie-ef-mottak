package no.nav.familie.ef.mottak.api.dto

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class SistInnsendteSøknadDto(
    val søknadsdato: LocalDate,
    val stønadType: String,
)

fun SistInnsendteSøknadDto.gjelderSøknad(dager: Long = 30): Boolean {
    val dagensDato = LocalDate.now()
    val dagerSidenInnsending = ChronoUnit.DAYS.between(søknadsdato, dagensDato)
    return dagerSidenInnsending <= dager
}
