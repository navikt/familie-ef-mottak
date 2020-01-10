package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.arkivering.Dokument
import no.nav.familie.kontrakter.felles.arkivering.FilType

object ArkiverDokumentRequestMapper {

    private const val DOKUMENTTYPE_OVERGANGSSTØNAD = "OVERGANGSSTØNAD_SØKNAD"
    private const val DOKUMENTTYPE_VEDLEGG = "OVERGANGSSTØNAD_SØKNAD_VEDLEGG"

    fun toDto(soknad: Soknad): ArkiverDokumentRequest {
        val dokumenter: List<Dokument> = tilDokumenter(soknad)
        return ArkiverDokumentRequest(soknad.fnr, true, dokumenter)
    }

    private fun tilDokumenter(soknad: Soknad): List<Dokument> {
        val vedleggsdokumenter = soknad.vedlegg.map { tilDokument(it) }
        val søknadsdokumentPdf =
                Dokument(soknad.søknadPdf.bytes, FilType.PDFA, null, null, DOKUMENTTYPE_OVERGANGSSTØNAD)
        val søknadsdokumentJson =
                Dokument(soknad.søknadJson.toByteArray(), FilType.JSON, null, null, DOKUMENTTYPE_OVERGANGSSTØNAD)
        return vedleggsdokumenter.plus(søknadsdokumentJson).plus(søknadsdokumentPdf)
    }

    private fun tilDokument(vedlegg: Vedlegg): Dokument {
        return Dokument(vedlegg.data.bytes, FilType.PDFA, vedlegg.filnavn, vedlegg.tittel, DOKUMENTTYPE_VEDLEGG)
    }


}