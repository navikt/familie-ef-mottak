package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.ArkivClient
import no.nav.familie.ef.mottak.integration.dto.ArkiverDokumentRequest
import no.nav.familie.ef.mottak.integration.dto.Dokument
import no.nav.familie.ef.mottak.integration.dto.DokumentType
import no.nav.familie.ef.mottak.integration.dto.Filtype
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JournalføringService(private val arkivClient: ArkivClient,
                           private val søknadService: SøknadService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun journalførSøknad(søknadId: String) {
        val soknad: Soknad = søknadService.get(søknadId.toLong())

        val journalpostId: String = send(soknad)

        val søknadMedJournalpostId = soknad.copy(journalpostId = journalpostId, vedlegg = emptyList())
        søknadService.lagreSøknad(søknadMedJournalpostId)
    }

    private fun send(soknad: Soknad): String {
        val dokumenter: List<Dokument> = tilDokumenter(soknad)
        val arkiverDokumentRequest = ArkiverDokumentRequest(soknad.fnr, true, dokumenter)
        val arkiverDokumentResponse = arkivClient.arkiver(arkiverDokumentRequest)

        return arkiverDokumentResponse.journalpostId
    }

    fun tilDokumenter(soknad: Soknad): List<Dokument> {
        val vedleggsdokumenter = soknad.vedlegg.map { tilDokument(it) }
        val søknadsdokument = Dokument(soknad.søknadPdf.bytes, Filtype.PDFA, DokumentType.OVERGANGSSTØNAD_SØKNAD)

        return vedleggsdokumenter.plus(søknadsdokument)
    }

    private fun tilDokument(vedlegg: Vedlegg): Dokument {
        return Dokument(vedlegg.data.bytes, Filtype.PDFA, DokumentType.OVERGANGSSTØNAD_SØKNAD_VEDLEGG, vedlegg.filnavn)
    }
}