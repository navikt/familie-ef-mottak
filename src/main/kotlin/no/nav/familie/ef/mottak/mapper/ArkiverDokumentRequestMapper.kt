package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_VEDLEGG
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.kontrakter.felles.arkivering.Dokument
import no.nav.familie.kontrakter.felles.arkivering.FilType
import no.nav.familie.kontrakter.felles.arkivering.v2.ArkiverDokumentRequest

object ArkiverDokumentRequestMapper {

    fun toDto(soknad: Soknad,
              vedlegg: List<Vedlegg>): ArkiverDokumentRequest {
        val søknadsdokumentJson =
                Dokument(soknad.søknadJson.toByteArray(), FilType.JSON, null, "hoveddokument", soknad.dokumenttype)
        val søknadsdokumentPdf =
                Dokument(soknad.søknadPdf!!.bytes, FilType.PDFA, null, "hoveddokument", soknad.dokumenttype)
        val hoveddokumentvarianter = listOf(søknadsdokumentPdf, søknadsdokumentJson)
        return ArkiverDokumentRequest(soknad.fnr, false, hoveddokumentvarianter, mapVedlegg(vedlegg))
    }

    private fun mapVedlegg(vedlegg: List<Vedlegg>): List<Dokument> {
        return vedlegg.map { tilDokument(it) }
    }

    private fun tilDokument(vedlegg: Vedlegg): Dokument {
        return Dokument(dokument = vedlegg.innhold.bytes,
                        filType = FilType.PDFA,
                        tittel = vedlegg.tittel,
                        filnavn = vedlegg.navn,
                        dokumentType = DOKUMENTTYPE_VEDLEGG)
    }


}
