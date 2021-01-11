package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.OpprettOppgaveMapper
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.oppgave.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI

@Service
class OppgaveService(private val integrasjonerClient: IntegrasjonerClient,
                     private val søknadService: SøknadService,
                     private val opprettOppgaveMapper: OpprettOppgaveMapper) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun lagJournalføringsoppgaveForSøknadId(søknadId: String): Long? {
        val soknad: Soknad = søknadService.get(søknadId)
        val journalpostId: String = soknad.journalpostId ?: error("Søknad mangler journalpostId")
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        return lagJournalføringsoppgave(journalpost)
    }

    fun lagJournalføringsoppgaveForJournalpostId(journalpostId: String): Long? {
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        return lagJournalføringsoppgave(journalpost)
    }

    fun lagBehandleSakOppgave(journalpost: Journalpost): Long {
        val opprettOppgave = opprettOppgaveMapper.toBehandleSakOppgave(journalpost)
        val nyOppgave = integrasjonerClient.lagOppgave(opprettOppgave)

        log.info("Oppretter ny behandle-sak-oppgave med oppgaveId=${nyOppgave.oppgaveId} " +
                 "for journalpost journalpostId=${journalpost.journalpostId}")

        return nyOppgave.oppgaveId
    }

    fun oppdaterOppgave(oppgaveId: Long, saksblokk:String, saksnummer: String): Long {
        val oppgave: Oppgave = integrasjonerClient.hentOppgave(oppgaveId)
        val oppdatertOppgave = oppgave.copy(saksreferanse = saksnummer,
                                beskrivelse = "${oppgave.beskrivelse} - Saksblokk: $saksblokk, Saksnummer: $saksnummer")
        return integrasjonerClient.oppdaterOppgave(oppgaveId, oppdatertOppgave)
    }

    fun lagJournalføringsoppgave(journalpost: Journalpost): Long? {

        if (journalpost.journalstatus == Journalstatus.MOTTATT) {
            return when {
                integrasjonerClient.finnOppgaver(journalpost.journalpostId, Oppgavetype.Journalføring).antallTreffTotalt > 0L -> {
                    log.info("Skipper oppretting av journalførings-oppgave. " +
                             "Fant åpen oppgave av type ${Oppgavetype.Journalføring} for " +
                             "journalpostId=${journalpost.journalpostId}")
                    null
                }
                integrasjonerClient.finnOppgaver(journalpost.journalpostId, Oppgavetype.Fordeling).antallTreffTotalt > 0L -> {
                    log.info("Skipper oppretting av journalførings-oppgave. " +
                             "Fant åpen oppgave av type ${Oppgavetype.Fordeling} for " +
                             "journalpostId=${journalpost.journalpostId}")
                    null
                }
                else -> {
                    val opprettOppgave = opprettOppgaveMapper.toDto(journalpost)

                    val nyOppgave = integrasjonerClient.lagOppgave(opprettOppgave)

                    log.info("Oppretter ny journalførings-oppgave med oppgaveId=${nyOppgave.oppgaveId} " +
                             "for journalpost journalpostId=${journalpost.journalpostId}")
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

    fun ferdigstillOppgaveForJournalpost(journalpostId: String) {
        val oppgaver = integrasjonerClient.finnOppgaver(journalpostId, Oppgavetype.Journalføring)
        when (oppgaver.antallTreffTotalt) {
            1L -> {
                val oppgaveId = oppgaver.oppgaver.first().id ?: error("Finner ikke oppgaveId for journalpost=$journalpostId")
                integrasjonerClient.ferdigstillOppgave(oppgaveId)
            }
            else -> {
                val error = IllegalStateException("Fant ${oppgaver.antallTreffTotalt} oppgaver for journalpost=$journalpostId")
                log.warn("Kan ikke ferdigstille oppgave", error)
                throw error
            }
        }
    }

}
