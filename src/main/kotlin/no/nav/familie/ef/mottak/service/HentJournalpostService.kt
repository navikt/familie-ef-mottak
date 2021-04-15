package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.springframework.stereotype.Service

@Service
class HentJournalpostService(private val søknadService: SøknadService,
                             private val integrasjonerClient: IntegrasjonerClient) {

    fun hentSaksnummer(søknadId: String) {
        val søknad: Søknad = søknadService.get(søknadId)
        val journalpostId: String = søknad.journalpostId ?: error("Søknad mangler journalpostId")
        val saksnummer = integrasjonerClient.hentSaksnummer(journalpostId)
        val søknadMedSaksnummer = søknad.copy(saksnummer = saksnummer)
        søknadService.lagreSøknad(søknadMedSaksnummer)
    }

    fun harSaksnummer(journalpostId: String): Boolean {
        return !integrasjonerClient.hentSaksnummer(journalpostId).isNullOrBlank()
    }

}