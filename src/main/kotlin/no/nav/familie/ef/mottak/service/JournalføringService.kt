package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.ArkivClient
import no.nav.familie.ef.mottak.mapper.ArkiverDokumentRequestMapper
import no.nav.familie.ef.mottak.repository.domain.Soknad
import org.springframework.stereotype.Service

@Service
class JournalføringService(private val arkivClient: ArkivClient,
                           private val søknadService: SøknadService) {

    fun journalførSøknad(søknadId: String) {
        val soknad: Soknad = søknadService.get(søknadId.toLong())
        val journalpostId: String = send(soknad)
        val søknadMedJournalpostId = soknad.copy(journalpostId = journalpostId, vedlegg = emptyList())

        søknadService.lagreSøknad(søknadMedJournalpostId)
    }

    private fun send(soknad: Soknad): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.toDto(soknad)
        val arkiverDokumentResponse = arkivClient.arkiver(arkiverDokumentRequest)

        return arkiverDokumentResponse.journalpostId
    }
}