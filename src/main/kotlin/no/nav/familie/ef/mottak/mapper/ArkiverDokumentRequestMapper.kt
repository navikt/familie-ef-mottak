package no.nav.familie.ef.mottak.mapper

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_VEDLEGG
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import no.nav.familie.kontrakter.felles.arkivering.Dokument
import no.nav.familie.kontrakter.felles.arkivering.FilType
import no.nav.familie.kontrakter.felles.arkivering.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.objectMapper

object ArkiverDokumentRequestMapper {

    fun toDto(soknad: Soknad): ArkiverDokumentRequest {
        val søknadsdokumentJson =
                Dokument(soknad.søknadJson.toByteArray(), FilType.JSON, null, "hoveddokument", soknad.dokumenttype)
        val søknadsdokumentPdf =
                Dokument(soknad.søknadPdf!!.bytes, FilType.PDFA, null, "hoveddokument", soknad.dokumenttype)
        val hoveddokumentvarianter = listOf(søknadsdokumentPdf, søknadsdokumentJson)
        return ArkiverDokumentRequest(soknad.fnr, false, hoveddokumentvarianter, mapVedlegg(soknad))
    }

    private fun mapVedlegg(soknad: Soknad): List<Dokument> {
        if (soknad.vedlegg == null) return emptyList()
        return objectMapper.readValue<List<Vedlegg>>(soknad.vedlegg).map { tilDokument(it) }
    }

    private fun tilDokument(vedlegg: Vedlegg): Dokument {
        return Dokument(dokument = vedlegg.bytes,
                        filType = FilType.PDFA,
                        tittel = vedlegg.tittel,
                        filnavn = null,
                        dokumentType = DOKUMENTTYPE_VEDLEGG)
    }


}
