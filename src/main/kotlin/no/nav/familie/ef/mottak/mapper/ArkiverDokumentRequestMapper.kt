package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.config.*
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.FilType

object ArkiverDokumentRequestMapper {

    fun toDto(soknad: Soknad,
              vedlegg: List<Vedlegg>): ArkiverDokumentRequest {
        val søknadsdokumentJson =
                Dokument(soknad.søknadJson.toByteArray(), FilType.JSON, null, "hoveddokument", soknad.dokumenttype)
        val søknadsdokumentPdf =
                Dokument(soknad.søknadPdf!!.bytes, FilType.PDFA, null, "hoveddokument", soknad.dokumenttype)
        val hoveddokumentvarianter = listOf(søknadsdokumentPdf, søknadsdokumentJson)
        return ArkiverDokumentRequest(soknad.fnr,
                                      false,
                                      hoveddokumentvarianter,
                                      mapVedlegg(vedlegg, soknad.dokumenttype))
    }

    private fun mapVedlegg(vedlegg: List<Vedlegg>, dokumenttype: String): List<Dokument> {
        if (vedlegg.isEmpty()) return emptyList()
        val dokumenttypeVedlegg = mapDokumenttype(dokumenttype)
        return vedlegg.map { tilDokument(it, dokumenttypeVedlegg) }
    }

    private fun tilDokument(vedlegg: Vedlegg, dokumenttypeVedlegg: String): Dokument {
        return Dokument(dokument = vedlegg.innhold.bytes,
                        filType = FilType.PDFA,
                        tittel = vedlegg.tittel,
                        filnavn = vedlegg.navn,
                        dokumentType = dokumenttypeVedlegg)
    }

    private fun mapDokumenttype(dokumenttype: String): String {
        return when (dokumenttype) {
            DOKUMENTTYPE_OVERGANGSSTØNAD -> DOKUMENTTYPE_OVERGANGSSTØNAD_VEDLEGG
            DOKUMENTTYPE_BARNETILSYN -> DOKUMENTTYPE_BARNETILSYN_VEDLEGG
            DOKUMENTTYPE_SKOLEPENGER -> DOKUMENTTYPE_SKOLEPENGER_VEDLEGG
            else -> error("Ukjent dokumenttype=$dokumenttype for vedlegg")
        }
    }

}
