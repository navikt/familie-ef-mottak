package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.SøknadTreeWalker
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.arkivering.Dokument
import no.nav.familie.kontrakter.felles.arkivering.FilType

object ArkiverDokumentRequestMapper {

    private const val DOKUMENTTYPE_OVERGANGSSTØNAD = "OVERGANGSSTØNAD_SØKNAD"
    private const val DOKUMENTTYPE_VEDLEGG = "OVERGANGSSTØNAD_SØKNAD_VEDLEGG"

    fun toDto(soknad: Soknad): ArkiverDokumentRequest {
        val kontrakssøknad = SøknadMapper.toDto(soknad)
        val vedleggsdokumenter = SøknadTreeWalker.finnDokumenter(kontrakssøknad).map { tilDokument(it) }
        //   val søknadsdokumentPdf = TODO genererPdf
        val søknadsdokumentJson =
                Dokument(soknad.søknadJson.toByteArray(), FilType.JSON, null, null, DOKUMENTTYPE_OVERGANGSSTØNAD)
        val dokumenter: List<Dokument> = vedleggsdokumenter.plus(søknadsdokumentJson)
        // .plus(søknadsdokumentPdf) TODO legg til generert pdf
        return ArkiverDokumentRequest(soknad.fnr, true, dokumenter)
    }

    private fun tilDokument(vedlegg: no.nav.familie.kontrakter.ef.søknad.Dokument): Dokument {
        return Dokument(dokument = vedlegg.fil.bytes,
                        filType = FilType.PDFA,
                        tittel = vedlegg.tittel,
                        filnavn = null,
                        dokumentType = DOKUMENTTYPE_VEDLEGG)
    }


}
