package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYNSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYNSTØNAD_VEDLEGG
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_VEDLEGG
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.kontrakter.felles.arkivering.Dokument
import no.nav.familie.kontrakter.felles.arkivering.FilType
import no.nav.familie.kontrakter.felles.arkivering.v2.ArkiverDokumentRequest

object ArkiverDokumentRequestMapper {

    fun toDto(soknad: Soknad, vedlegg: List<Vedlegg>): ArkiverDokumentRequest {
        val søknadsdokumentJson =
                Dokument(soknad.søknadJson.toByteArray(), FilType.JSON, null, "hoveddokument", soknad.dokumenttype)
        val søknadsdokumentPdf =
                Dokument(soknad.søknadPdf!!.bytes, FilType.PDFA, null, "hoveddokument", soknad.dokumenttype)
        val hoveddokumentvarianter = listOf(søknadsdokumentPdf, søknadsdokumentJson)
        return ArkiverDokumentRequest(soknad.fnr, false, hoveddokumentvarianter, mapVedlegg(vedlegg, soknad.dokumenttype))
    }

    private fun mapVedlegg(vedlegg: List<Vedlegg>,
                           dokumenttype: String): List<Dokument> {
        return vedlegg.map { tilDokument(it, dokumenttype) }
    }

    private fun tilDokument(vedlegg: Vedlegg, dokumenttype: String): Dokument {

        val dokumentType = when (dokumenttype) {
            DOKUMENTTYPE_OVERGANGSSTØNAD -> DOKUMENTTYPE_VEDLEGG
            DOKUMENTTYPE_BARNETILSYNSTØNAD -> DOKUMENTTYPE_BARNETILSYNSTØNAD_VEDLEGG
            else -> {
                error("Ukjent dokumenttype for vedlegg ")
            }
        }

        return Dokument(dokument = vedlegg.innhold.bytes,
                        filType = FilType.PDFA,
                        tittel = vedlegg.tittel,
                        filnavn = vedlegg.navn,
                        dokumentType = dokumentType)
    }


}
