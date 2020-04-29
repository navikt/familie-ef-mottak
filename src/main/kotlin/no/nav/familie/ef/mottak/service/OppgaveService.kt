package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.ArkiverDokumentRequestMapper
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.felles.oppgave.OppgaveIdent
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgave
import org.springframework.stereotype.Service

@Service
class OppgaveService(private val integrasjonerClient: IntegrasjonerClient,
                     private val søknadService: SøknadService) {

    fun lagOppgave(søknadId: String): String {
        val soknad: Soknad = søknadService.get(søknadId)
        val journalpostId: String = soknad.journalpostId ?: error("Søknad mangler journalpostId")
        OpprettOppgave(OppgaveIdent)

        integrasjonerClient.lagOppgave()
        val søknadMedSaksnummer = soknad.copy(saksnummer = saksnummer) // ???? Skal vi lager noe her?
        søknadService.lagreSøknad(søknadMedSaksnummer)
    }

    private fun send(soknad: Soknad): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.toDto(soknad)
        val ressurs = integrasjonerClient.arkiver(arkiverDokumentRequest)
        return ressurs.journalpostId
    }
}
