package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.OpprettOppgaveMapper
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException

@Service
class OppgaveService(private val integrasjonerClient: IntegrasjonerClient,
                     private val søknadService: SøknadService,
                     private val opprettOppgaveMapper: OpprettOppgaveMapper) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")
    private val ENHETSNUMMER_NAY: String = "4489"

    fun lagJournalføringsoppgaveForSøknadId(søknadId: String): Long? {
        val soknad: Soknad = søknadService.get(søknadId)
        val journalpostId: String = soknad.journalpostId ?: error("Søknad mangler journalpostId")
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        return lagJournalføringsoppgave(journalpost)
    }

    fun lagJournalføringsoppgaveForJournalpostId(journalpostId: String): Long? {
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        try {
            return lagJournalføringsoppgave(journalpost)
        } catch (e: Exception) {
            secureLogger.warn("Kunne ikke opprette journalføringsoppgave for journalpost=$journalpost", e)
            throw e
        }
    }

    fun lagBehandleSakOppgave(journalpost: Journalpost, behandlesAvApplikasjon: String): Long {
        val opprettOppgave = opprettOppgaveMapper.toBehandleSakOppgave(journalpost, behandlesAvApplikasjon)
        val nyOppgave = integrasjonerClient.lagOppgave(opprettOppgave)

        log.info("Oppretter ny behandle-sak-oppgave med oppgaveId=${nyOppgave.oppgaveId} " +
                 "for journalpost journalpostId=${journalpost.journalpostId}")

        return nyOppgave.oppgaveId
    }

    fun oppdaterOppgave(oppgaveId: Long, saksblokk: String, saksnummer: String, behandlesAvApplikasjon: String): Long {
        val oppgave: Oppgave = integrasjonerClient.hentOppgave(oppgaveId)
        val oppdatertOppgave = oppgave.copy(
                saksreferanse = saksnummer,
                beskrivelse = "${oppgave.beskrivelse} - Saksblokk: $saksblokk, Saksnummer: $saksnummer [Automatisk journalført]",
                behandlesAvApplikasjon = behandlesAvApplikasjon
        )
        return integrasjonerClient.oppdaterOppgave(oppgaveId, oppdatertOppgave)
    }

    fun lagJournalføringsoppgave(journalpost: Journalpost): Long? {

        if (journalpost.journalstatus == Journalstatus.MOTTATT) {
            return when {
                journalføringsoppgaveFinnes(journalpost) -> {
                    loggSkipOpprettOppgave(journalpost.journalpostId, Oppgavetype.Journalføring)
                    null
                }
                fordelingsoppgaveFinnes(journalpost) -> {
                    loggSkipOpprettOppgave(journalpost.journalpostId, Oppgavetype.Fordeling)
                    null
                }
                behandlesakOppgaveFinnes(journalpost) -> {
                    loggSkipOpprettOppgave(journalpost.journalpostId, Oppgavetype.BehandleSak)
                    null
                }
                else -> {
                    val opprettOppgave = opprettOppgaveMapper.toDto(journalpost)
                    return opprettOppgaveMedEnhetFraNorgEllerBrukNayHvisEnhetIkkeFinnes(opprettOppgave, journalpost)
                }
            }
        } else {
            val error = IllegalStateException("Journalpost ${journalpost.journalpostId} har endret status " +
                                              "fra MOTTATT til ${journalpost.journalstatus.name}")
            log.info("OpprettJournalføringOppgaveTask feilet.", error)
            throw error
        }
    }

    private fun opprettOppgaveMedEnhetFraNorgEllerBrukNayHvisEnhetIkkeFinnes(opprettOppgave: OpprettOppgaveRequest,
                                                                             journalpost: Journalpost): Long? {
        return try {
            val nyOppgave = integrasjonerClient.lagOppgave(opprettOppgave)
            log.info("Oppretter ny journalførings-oppgave med oppgaveId=${nyOppgave.oppgaveId} for journalpost journalpostId=${journalpost.journalpostId}")
            nyOppgave.oppgaveId
        } catch (httpStatusCodeException: HttpStatusCodeException) {
            if (finnerIngenGyldigArbeidsfordelingsenhetForBruker(httpStatusCodeException)) {
                val nyOppgave = integrasjonerClient.lagOppgave(opprettOppgave.copy(enhetsnummer = ENHETSNUMMER_NAY))
                log.info("Oppretter ny journalførings-oppgave med oppgaveId=${nyOppgave.oppgaveId} for journalpost journalpostId=${journalpost.journalpostId} med enhetsnummer=$ENHETSNUMMER_NAY")
                nyOppgave.oppgaveId
            } else {
                throw httpStatusCodeException
            }
        }
    }

    private fun finnerIngenGyldigArbeidsfordelingsenhetForBruker(httpStatusCodeException: HttpStatusCodeException): Boolean {
        try {
            val response: Ressurs<OppgaveResponse> = objectMapper.readValue(httpStatusCodeException.responseBodyAsString)
            val feilmelding = response.melding
            secureLogger.warn("Feil ved oppretting av oppgave $feilmelding")
            return feilmelding.contains("Fant ingen gyldig arbeidsfordeling for oppgaven")
        } catch (e: Exception) {
            secureLogger.error("Feilet ved parsing av feilstatus", e)
            throw httpStatusCodeException
        }

    }

    private fun loggSkipOpprettOppgave(journalpostId: String, oppgavetype: Oppgavetype) {
        log.info("Skipper oppretting av journalførings-oppgave. " +
                 "Fant åpen oppgave av type ${oppgavetype} for " +
                 "journalpostId=${journalpostId}")
    }

    private fun fordelingsoppgaveFinnes(journalpost: Journalpost) =
            integrasjonerClient.finnOppgaver(journalpost.journalpostId, Oppgavetype.Fordeling).antallTreffTotalt > 0L

    private fun journalføringsoppgaveFinnes(journalpost: Journalpost) =
            integrasjonerClient.finnOppgaver(journalpost.journalpostId, Oppgavetype.Journalføring).antallTreffTotalt > 0L

    private fun behandlesakOppgaveFinnes(journalpost: Journalpost) =
            integrasjonerClient.finnOppgaver(journalpost.journalpostId, Oppgavetype.BehandleSak).antallTreffTotalt > 0L

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
