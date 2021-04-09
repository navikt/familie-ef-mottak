package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.config.*
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.FilType

object ArkiverDokumentRequestMapper {

    fun toDto(søknad: Søknad,
              vedlegg: List<Vedlegg>): ArkiverDokumentRequest {
        val søknadsdokumentJson =
                Dokument(søknad.søknadJson.toByteArray(), FilType.JSON, null, "hoveddokument", søknad.dokumenttype)
        val søknadsdokumentPdf =
                Dokument(søknad.søknadPdf!!.bytes, FilType.PDFA, null, "hoveddokument", søknad.dokumenttype)
        val hoveddokumentvarianter = listOf(søknadsdokumentPdf, søknadsdokumentJson)
        return ArkiverDokumentRequest(søknad.fnr,
                                      false,
                                      hoveddokumentvarianter,
                                      mapVedlegg(vedlegg, søknad.dokumenttype))
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
                        filnavn = vedlegg.id.toString(),
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
