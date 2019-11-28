package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.ArkivClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
import org.springframework.stereotype.Service

@Service
class HentJournalpostService(private val søknadService: SøknadService,
                             private val arkivClient: ArkivClient) {

    fun hentSaksnummer(søknadId: String) {
        val soknad: Soknad = søknadService.get(søknadId.toLong())
        val journalpostId: String = soknad.journalpostId ?: error("Søknad mangler journalpostId")

        val saksnummer = arkivClient.hentSaksnummer(journalpostId)

        val søknadMedSaksnummer = soknad.copy(saksnummer = saksnummer)

        søknadService.lagreSøknad(søknadMedSaksnummer)
    }

    fun hentJournalpostId(søknadId: String, callId: String) {
        val soknad: Soknad = søknadService.get(søknadId.toLong())
        val journalpostId = arkivClient.hentJournalpostId(callId)

        val søknadMedJournalpostId = soknad.copy(journalpostId = journalpostId)

        søknadService.lagreSøknad(søknadMedJournalpostId)
    }
}