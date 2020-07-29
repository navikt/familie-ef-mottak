package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.domain.Vedlegg
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

    fun mapVedlegg(vedlegg: List<Vedlegg>): Map<String, String> {
        val verdi = vedlegg.sortedBy { it.tittel }.map { it.tittel }.joinToString("\n\n")
        return feltMap("Vedlegg", verdi)
    }

    private fun mapVerdi(verdi: Any): String {
        return when (verdi) {
            is Month ->
                displayName(verdi)
            is Boolean ->
                if (verdi) "Ja" else "Nei"
            is List<*> ->
                verdi.joinToString("\n\n") { mapVerdi(it!!) }
            is Fødselsnummer ->
                verdi.verdi
            is Adresse ->
                adresseString(verdi)
            is LocalDate ->
                verdi.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            is LocalDateTime ->
                verdi.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
            is Periode ->
                periodeString(verdi)
            else ->
                verdi.toString()
        }
    }

    private fun displayName(verdi: Month) = verdi.getDisplayName(TextStyle.FULL, Locale("no"))

    private fun periodeString(verdi: Periode): String {
        return "Fra ${displayName(verdi.fraMåned)} ${verdi.fraÅr} til ${displayName(verdi.tilMåned)} ${verdi.tilÅr}"
    }

    private fun adresseString(adresse: Adresse): String {
        return listOf(adresse.adresse,
                      listOf(adresse.postnummer, adresse.poststedsnavn).joinToString(" "),
                      adresse.land).joinToString("\n\n")
    }

    private fun feltMap(label: String, verdi: String) = mapOf("label" to label, "verdi" to verdi)

}
