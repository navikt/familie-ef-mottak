package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.integration.dto.ArkiverSøknadRequest
import no.nav.familie.ef.mottak.integration.dto.Dokument
import no.nav.familie.ef.mottak.integration.dto.DokumentType
import no.nav.familie.ef.mottak.integration.dto.Filtype
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg

object ArkiverDokumentRequestMapper {

    fun toDto(soknad: Soknad): ArkiverSøknadRequest {
        val dokumenter: List<Dokument> = tilDokumenter(soknad)
        return ArkiverSøknadRequest(soknad.fnr, true, dokumenter)
    }

    private fun tilDokumenter(soknad: Soknad): List<Dokument> {
        val vedleggsdokumenter = soknad.vedlegg.map { tilDokument(it) }
        val søknadsdokumentPdf = Dokument(soknad.søknadPdf.bytes, Filtype.PDFA, DokumentType.OVERGANGSSTØNAD_SØKNAD)
        val søknadsdokumentJson = Dokument(soknad.søknadJson.toByteArray(), Filtype.JSON, DokumentType.OVERGANGSSTØNAD_SØKNAD)
        return vedleggsdokumenter.plus(søknadsdokumentJson).plus(søknadsdokumentPdf)
    }

    private fun tilDokument(vedlegg: Vedlegg): Dokument {
        return Dokument(vedlegg.data.bytes, Filtype.PDFA, DokumentType.OVERGANGSSTØNAD_SØKNAD_VEDLEGG, vedlegg.filnavn)
    }


}