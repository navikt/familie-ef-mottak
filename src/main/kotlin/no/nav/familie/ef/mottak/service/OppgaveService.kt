package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.oppgave.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OppgaveService(private val integrasjonerClient: IntegrasjonerClient,
                     private val søknadService: SøknadService) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun lagJournalføringsoppgave(søknadId: String): Long? {
        val soknad: Soknad = søknadService.get(søknadId)
        if (soknad.dokumenttype == DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER) {
            val aktørId = integrasjonerClient.hentAktørId(soknad.fnr)

            val journalpostId: String = soknad.journalpostId ?: error("Søknad mangler journalpostId")

            val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
            if (journalpost.journalstatus == Journalstatus.MOTTATT) {
                return when {
                    finnOppgaver(journalpost.journalpostId, Oppgavetype.Journalføring).antallTreffTotalt > 0L -> {
                        log.info("Skipper oppretting av journalførings-oppgave. " +
                                 "Fant åpen oppgave av type ${Oppgavetype.Journalføring} for ${journalpost.journalpostId}")
                        null
                    }
                    finnOppgaver(journalpost.journalpostId, Oppgavetype.Fordeling).antallTreffTotalt > 0L -> {
                        log.info("Skipper oppretting av journalførings-oppgave. " +
                                 "Fant åpen oppgave av type ${Oppgavetype.Fordeling} for ${journalpost.journalpostId}")
                        null
                    }
                    else -> {
                        val nyOppgave = lagJournalføringsoppgave(journalpostId, aktørId)
                        log.info("Oppretter ny journalførings-oppgave med id ${nyOppgave.oppgaveId} " +
                                 "for journalpost ${journalpost.journalpostId}")
                        nyOppgave.oppgaveId
                    }
                }
            } else {
                val error = IllegalStateException("Journalpost ${journalpost.journalpostId} har endret status " +
                                                  "fra MOTTATT til ${journalpost.journalstatus.name}")
                log.info("OpprettJournalføringOppgaveTask feilet.", error)
                throw error
            }
        }
        return null
    }

    fun lagJournalføringsoppgave(journalpostId: String, aktørId: String): OppgaveResponse {
        val opprettOppgave = OpprettOppgave(ident = OppgaveIdent(ident = aktørId, type = IdentType.Aktør),
                                            saksId = null,
                                            journalpostId = journalpostId,
                                            tema = Tema.ENF,
                                            oppgavetype = Oppgavetype.Journalføring,
                                            fristFerdigstillelse = LocalDate.now(),
                                            beskrivelse = "Enslig mor eller far som er arbeidssøker (15-08.01)",
                                            behandlingstema = null,
                                            enhetsnummer = null)

        return integrasjonerClient.lagOppgave(opprettOppgave)

    }

    fun finnOppgaver(journalpostId: String, oppgavetype: Oppgavetype?): FinnOppgaveResponseDto {
        val finnOppgaveRequest = FinnOppgaveRequest(tema = Tema.ENF, journalpostId = journalpostId, oppgavetype = oppgavetype)
        return integrasjonerClient.finnOppgaver(finnOppgaveRequest)
    }

}
