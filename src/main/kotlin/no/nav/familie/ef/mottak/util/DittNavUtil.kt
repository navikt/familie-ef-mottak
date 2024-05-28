package no.nav.familie.ef.mottak.util

import no.nav.familie.kontrakter.ef.søknad.SøknadType
import java.net.URL

data class LinkMelding(val link: URL, val melding: String)

fun lagMeldingPåminnelseManglerDokumentasjonsbehov(
    ettersendingURL: URL,
    dittNavTekst: String,
) = LinkMelding(
    link = ettersendingURL,
    melding =
        "Det ser ut til at det mangler noen vedlegg til søknaden din om $dittNavTekst." +
            " Se hva som mangler og last opp vedlegg. Dersom du allerede har sendt inn den manglende" +
            " dokumentasjonen, så kan du se bort fra denne meldingen.",
)

fun lagMeldingManglerDokumentasjonsbehov(
    ettersendingURL: URL,
    dittNavTekst: String,
) = LinkMelding(
    link = ettersendingURL,
    melding =
        "Det ser ut til at det mangler noen vedlegg til søknaden din om $dittNavTekst." +
            " Se hva som mangler og last opp vedlegg.",
)

fun lagMeldingSøknadMottattBekreftelse(
    ettersendingURL: URL,
    dittNavTekst: String,
) = LinkMelding(
    link = ettersendingURL,
    melding = "Vi har mottatt søknaden din om $dittNavTekst.",
)

fun tilDittNavTekst(søknadType: SøknadType): String {
    return when (søknadType) {
        SøknadType.BARNETILSYN -> "stønad til barnetilsyn"
        SøknadType.OVERGANGSSTØNAD -> "overgangsstønad"
        SøknadType.SKOLEPENGER -> "stønad til skolepenger"
        else -> error("Kan ikke mappe dokumenttype $søknadType til dittnav tekst")
    }
}
