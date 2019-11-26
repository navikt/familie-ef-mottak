package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.ArkivClient
import no.nav.familie.ef.mottak.integration.dto.ArkiverDokumentRequest
import no.nav.familie.ef.mottak.integration.dto.Dokument
import no.nav.familie.ef.mottak.integration.dto.DokumentType
import no.nav.familie.ef.mottak.integration.dto.Filtype
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JournalføringService(private val arkivClient: ArkivClient,
                           private val søknadService: SøknadService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun journalførSøknad(søknadId: String) {
        val søknad: Søknad = søknadService.get(søknadId.toLong())

        val journalpostId: String = send(søknad)

        val søknadMedJournalpostId = søknad.copy(journalpostId = journalpostId, vedlegg = emptyList())
        søknadService.lagreSøknad(søknadMedJournalpostId)
    }

    private fun send(søknad: Søknad): String {
        val dokumenter: List<Dokument> = tilDokumenter(søknad)
        val arkiverDokumentRequest = ArkiverDokumentRequest(søknad.fnr, true, dokumenter)
        val arkiverDokumentResponse = arkivClient.arkiver(arkiverDokumentRequest)

        return arkiverDokumentResponse.journalpostId
    }

    fun tilDokumenter(søknad: Søknad): List<Dokument> {
        val vedleggsdokumenter = søknad.vedlegg.map { tilDokument(it) }
        val søknadsdokument = Dokument(søknad.søknadPdf.bytes, Filtype.PDFA, DokumentType.OVERGANGSSTØNAD_SØKNAD)

        return vedleggsdokumenter.plus(søknadsdokument)
    }

    private fun tilDokument(vedlegg: Vedlegg): Dokument {
        return Dokument(vedlegg.data, Filtype.PDFA, DokumentType.OVERGANGSSTØNAD_SØKNAD_VEDLEGG, vedlegg.filnavn)
    }
}