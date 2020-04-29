package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.felles.oppgave.*
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OppgaveService(private val integrasjonerClient: IntegrasjonerClient,
                     private val søknadService: SøknadService) {

    fun lagOppgave(søknadId: String) {
        val soknad: Soknad = søknadService.get(søknadId)
        if (soknad.dokumenttype == DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER) {
            val aktørId = integrasjonerClient.hentAktørId(soknad.fnr)
            val fristForFerdigstillelse = LocalDate.now()
            val beskrivelse = "Oppgavetekst skal inn her - hva skal stå her"

            val journalpostId: String = soknad.journalpostId ?: error("Søknad mangler journalpostId")
            val opprettOppgave = OpprettOppgave(
                    ident = OppgaveIdent(ident = aktørId, type = IdentType.Aktør),
                    saksId = journalpostId,
                    tema = Tema.ENF,
                    oppgavetype = Oppgavetype.Journalføring,
                    fristFerdigstillelse = fristForFerdigstillelse,
                    beskrivelse = beskrivelse,
                    behandlingstema = null,
                    enhetsnummer = null
            )

            integrasjonerClient.lagOppgave(opprettOppgave);
        }
    }
}
