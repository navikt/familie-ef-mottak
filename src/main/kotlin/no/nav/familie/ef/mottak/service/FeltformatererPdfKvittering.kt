package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.domain.VerdilisteElement
import no.nav.familie.kontrakter.ef.søknad.Adresse
import no.nav.familie.kontrakter.ef.søknad.Datoperiode
import no.nav.familie.kontrakter.ef.søknad.MånedÅrPeriode
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
import no.nav.familie.kontrakter.felles.Fødselsnummer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object FeltformatererPdfKvittering {
    /**
     * Håndterer formatering utover vanlig toString for endenodene
     */
    fun genereltFormatMapperMapEndenode(entitet: Søknadsfelt<*>): VerdilisteElement? {
        // skal ekskluderes
        val skalEkskluderes =
            ((entitet.label == "Jeg har sendt inn denne dokumentasjonen til Nav tidligere" || entitet.label == "I have already submitted this documentation to Nav in the past") && entitet.verdi.toString() == "false") ||
                (entitet.label == "Født" && entitet.verdi.toString() == "true")

        if (skalEkskluderes) {
            return null
        }
        return mapTilVerdiListeElement(entitet)
    }

    fun mapVedlegg(vedleggTitler: List<String>): VerdilisteElement = VerdilisteElement("Vedlegg", verdi = vedleggTitler.joinToString("\n\n"))

    private fun mapTilVerdiListeElement(entitet: Søknadsfelt<*>) =
        VerdilisteElement(
            entitet.label,
            verdi = mapVerdi(entitet.verdi!!),
            alternativer = entitet.alternativer?.joinToString(" / "),
        )

    private fun mapVerdi(verdi: Any): String =
        when (verdi) {
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

    private fun tilUtskriftsformat(verdi: Boolean) = if (verdi) "Ja" else "Nei"

    private fun tilUtskriftsformat(verdi: Double) = String.format("%.2f", verdi).replace(".", ",")

    private fun tilUtskriftsformat(verdi: Month) = verdi.getDisplayName(TextStyle.FULL, Locale("no"))

    private fun tilUtskriftsformat(verdi: LocalDateTime) = verdi.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))

    private fun tilUtskriftsformat(verdi: MånedÅrPeriode): String = "Fra ${tilUtskriftsformat(verdi.fraMåned)} ${verdi.fraÅr} til ${tilUtskriftsformat(verdi.tilMåned)} ${verdi.tilÅr}"

    private fun tilUtskriftsformat(verdi: Datoperiode): String = "Fra ${tilUtskriftsformat(verdi.fra)} til ${tilUtskriftsformat(verdi.til)}"

    private fun tilUtskriftsformat(verdi: LocalDate): String = verdi.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

    private fun tilUtskriftsformat(adresse: Adresse): String {
        val adresseelementer =
            listOfNotNull(
                adresse.adresse?.takeIf { it.isNotBlank() },
                listOfNotNull(adresse.postnummer, adresse.poststedsnavn)
                    .joinToString(" ") { it.trim() }
                    .takeIf { it.isNotBlank() },
                adresse.land?.takeIf { it.isNotBlank() },
            )

        return if (adresseelementer.isEmpty()) {
            "Ingen registrert adresse"
        } else {
            adresseelementer.joinToString("\n")
        }
    }
}
