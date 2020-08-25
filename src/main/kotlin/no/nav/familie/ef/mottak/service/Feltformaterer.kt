package no.nav.familie.ef.mottak.service

import no.nav.familie.kontrakter.ef.søknad.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

object Feltformaterer {

    /**
     * Håndterer formatering utover vanlig toString for endenodene
     */
    fun mapEndenodeTilUtskriftMap(entitet: Søknadsfelt<*>): Map<String, String> {
        return feltMap(entitet.label, mapVerdi(entitet.verdi!!))
    }

    fun mapVedlegg(vedleggTitler: List<String>): Map<String, String> {
        val verdi = vedleggTitler.joinToString("\n\n")
        return feltMap("Vedlegg", verdi)
    }

    private fun mapVerdi(verdi: Any): String {
        return when (verdi) {
            is Month ->
                displayName(verdi)
            is Boolean ->
                if (verdi) "Ja" else "Nei"
            is Double ->
                String.format("%.2f", verdi).replace(".", ",")
            is List<*> ->
                verdi.joinToString("\n\n") { mapVerdi(it!!) }
            is Fødselsnummer ->
                verdi.verdi
            is Adresse ->
                adresseString(verdi)
            is LocalDate ->
                datoverdi(verdi)
            is LocalDateTime ->
                verdi.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
            is MånedÅrPeriode ->
                månedÅrPeriodeString(verdi)
            is Datoperiode ->
                datoPeriodeString(verdi)
            else ->
                verdi.toString()
        }
    }

    private fun displayName(verdi: Month) = verdi.getDisplayName(TextStyle.FULL, Locale("no"))

    private fun månedÅrPeriodeString(verdi: MånedÅrPeriode): String {
        return "Fra ${displayName(verdi.fraMåned)} ${verdi.fraÅr} til ${displayName(verdi.tilMåned)} ${verdi.tilÅr}"
    }

    private fun datoPeriodeString(verdi: Datoperiode): String {
        return "Fra ${datoverdi(verdi.fra)} til ${datoverdi(verdi.til)}"
    }

    private fun datoverdi(verdi: LocalDate): String {
        return verdi.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    private fun adresseString(adresse: Adresse): String {
        return listOf(adresse.adresse,
                      listOf(adresse.postnummer, adresse.poststedsnavn).joinToString(" "),
                      adresse.land).joinToString("\n\n")
    }

    private fun feltMap(label: String, verdi: String) = mapOf("label" to label, "verdi" to verdi)

}
