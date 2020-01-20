package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.service.DokumentHelper
import no.nav.familie.kontrakter.ef.søknad.Søknad
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.arkivering.Dokument
import no.nav.familie.kontrakter.felles.arkivering.FilType
import no.nav.familie.kontrakter.felles.objectMapper

object ArkiverDokumentRequestMapper {

    private const val DOKUMENTTYPE_OVERGANGSSTØNAD = "OVERGANGSSTØNAD_SØKNAD"
    private const val DOKUMENTTYPE_VEDLEGG = "OVERGANGSSTØNAD_SØKNAD_VEDLEGG"

    fun toDto(søknad: Søknad): ArkiverDokumentRequest {
        val dokumenter: List<Dokument> = tilDokumenter(søknad)
        return ArkiverDokumentRequest(søknad.personalia.verdi.fødselsnummer.verdi.verdi, true, dokumenter)
    }

    private fun tilDokumenter(søknad: Søknad): List<Dokument> {
        val vedleggsdokumenter = DokumentHelper.finnDokumenter(søknad).map { tilDokument(it) }
        //   val søknadsdokumentPdf = TODO genererPdf
        val søknadsdokumentJson =
                Dokument(objectMapper.writeValueAsBytes(søknad), FilType.JSON, null, null, DOKUMENTTYPE_OVERGANGSSTØNAD)
        return vedleggsdokumenter.plus(søknadsdokumentJson)
        // .plus(søknadsdokumentPdf) TODO legg til generert pdf
    }

    private fun tilDokument(vedlegg: no.nav.familie.kontrakter.ef.søknad.Dokument): Dokument {
        return Dokument(dokument = vedlegg.fil.bytes,
                        filType = FilType.PDFA,
                        tittel = vedlegg.tittel,
                        filnavn = null,
                        dokumentType = DOKUMENTTYPE_VEDLEGG)
    }


}