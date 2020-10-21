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
        return feltMap(entitet.label, mapVerdi(entitet.verdi!!), entitet.alternativer)
    }

    fun mapVedlegg(vedleggTitler: List<String>): Map<String, String> {
        val verdi = vedleggTitler.joinToString("\n\n")
        return feltMap("Vedlegg", verdi)
    }

    private fun mapVerdi(verdi: Any): String {
        return when (verdi) {
            is Month ->
                tilUtskriftsformat(verdi)
            is Boolean ->
                tilUtskriftsformat(verdi)
            is Double ->
                tilUtskriftsformat(verdi)
            is List<*> ->
                verdi.joinToString("\n\n") { mapVerdi(it!!) }
            is Fødselsnummer ->
                verdi.verdi
            is Adresse ->
                tilUtskriftsformat(verdi)
            is LocalDate ->
                tilUtskriftsformat(verdi)
            is LocalDateTime ->
                tilUtskriftsformat(verdi)
            is MånedÅrPeriode ->
                tilUtskriftsformat(verdi)
            is Datoperiode ->
                tilUtskriftsformat(verdi)
            else ->
                verdi.toString()
        }
    }


    private fun tilUtskriftsformat(verdi: Boolean) = if (verdi) "Ja" else "Nei"
    private fun tilUtskriftsformat(verdi: Double) = String.format("%.2f", verdi).replace(".", ",")
    private fun tilUtskriftsformat(verdi: Month) = verdi.getDisplayName(TextStyle.FULL, Locale("no"))
    private fun tilUtskriftsformat(verdi: LocalDateTime) = verdi.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))

    private fun tilUtskriftsformat(verdi: MånedÅrPeriode): String {
        return "Fra ${tilUtskriftsformat(verdi.fraMåned)} ${verdi.fraÅr} til ${tilUtskriftsformat(verdi.tilMåned)} ${verdi.tilÅr}"
    }

    private fun tilUtskriftsformat(verdi: Datoperiode): String {
        return "Fra ${tilUtskriftsformat(verdi.fra)} til ${tilUtskriftsformat(verdi.til)}"
    }

    private fun tilUtskriftsformat(verdi: LocalDate): String {
        return verdi.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    private fun tilUtskriftsformat(adresse: Adresse): String {
        return listOf(adresse.adresse,
                      listOf(adresse.postnummer, adresse.poststedsnavn).joinToString(" "),
                      adresse.land).joinToString("\n\n")
    }

    private fun feltMap(label: String, verdi: String, alternativer: List<String>? = null): Map<String, String> {
        return if (alternativer != null) {
            mapOf("label" to label, "verdi" to verdi, "alternativer" to alternativer.joinToString(" / "))
        } else {
            mapOf("label" to label, "verdi" to verdi)
        }
    }

}
