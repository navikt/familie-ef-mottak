package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.ArkivClient
import no.nav.familie.ef.mottak.mapper.ArkiverDokumentRequestMapper
import no.nav.familie.ef.mottak.repository.domain.Soknad
import org.springframework.stereotype.Service

@Service
class JournalføringService(private val arkivClient: ArkivClient,
                           private val søknadService: SøknadService) {

    fun journalførSøknad(søknadId: String): String {
        val soknad: Soknad = søknadService.get(søknadId)
        val journalpostId: String = send(soknad)
        val søknadMedJournalpostId = soknad.copy(journalpostId = journalpostId)
        søknadService.lagreSøknad(søknadMedJournalpostId)
        return journalpostId
    }

    private fun send(soknad: Soknad): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.toDto(soknad)
        val ressurs = arkivClient.arkiver(arkiverDokumentRequest)
        return ressurs.journalpostId
    }
}
