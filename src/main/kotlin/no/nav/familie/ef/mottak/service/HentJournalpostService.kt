package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.ArkivClient
import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.springframework.stereotype.Service

@Service
class HentJournalpostService(private val søknadService: SøknadService,
                             private val arkivClient: ArkivClient) {

    fun hentSaksnummer(søknadId: String) {
        val søknad: Søknad = søknadService.get(søknadId.toLong())
        val journalpostId: String = søknad.journalpostId ?: error("Søknad mangler journalpostId")

        val saksnummer = arkivClient.hentSaksnummer(journalpostId)

        val søknadMedSaksnummer = søknad.copy(saksnummer = saksnummer)

        søknadService.lagreSøknad(søknadMedSaksnummer)
    }

    fun hentJournalpostId(søknadId: String, callId: String) {
        val søknad: Søknad = søknadService.get(søknadId.toLong())
        val journalpostId = arkivClient.hentJournalpostId(callId)

        val søknadMedJournalpostId = søknad.copy(journalpostId = journalpostId, vedlegg = emptyList())

        søknadService.lagreSøknad(søknadMedJournalpostId)
    }
}