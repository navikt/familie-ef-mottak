package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_VEDLEGG
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.SøknadTreeWalker
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.ef.søknad.Søknad
import no.nav.familie.kontrakter.felles.arkivering.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.arkivering.Dokument
import no.nav.familie.kontrakter.felles.arkivering.FilType

object ArkiverDokumentRequestMapper {

    fun toDto(soknad: Soknad): ArkiverDokumentRequest {

        val kontraktssøknad: Any =
                if (soknad.dokumenttype == DOKUMENTTYPE_OVERGANGSSTØNAD) {
                    SøknadMapper.toDto<Søknad>(soknad)
                } else {
                    SøknadMapper.toDto<SkjemaForArbeidssøker>(soknad)
                }

        val vedleggsdokumenter = SøknadTreeWalker.finnDokumenter(kontraktssøknad).map { tilDokument(it) }

        val søknadsdokumentJson =
                Dokument(soknad.søknadJson.toByteArray(), FilType.JSON, null, "hoveddokument", soknad.dokumenttype)
        val søknadsdokumentPdf =
                Dokument(soknad.søknadPdf!!.bytes, FilType.PDFA, null, "hoveddokument", soknad.dokumenttype)
        val hoveddokumentvarianter = listOf(søknadsdokumentPdf, søknadsdokumentJson)
        return ArkiverDokumentRequest(soknad.fnr, false, hoveddokumentvarianter, vedleggsdokumenter)
    }

    private fun tilDokument(vedlegg: no.nav.familie.kontrakter.ef.søknad.Dokument): Dokument {
        return Dokument(dokument = vedlegg.bytes,
                        filType = FilType.PDFA,
                        tittel = vedlegg.tittel,
                        filnavn = null,
                        dokumentType = DOKUMENTTYPE_VEDLEGG)
    }


}
