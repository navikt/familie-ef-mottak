package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.BehandlesAvApplikasjon
import no.nav.familie.ef.mottak.mapper.OpprettOppgaveMapper
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.util.UtledPrioritetForSøknadUtil
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgavePrioritet
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.StatusEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OppgaveService(
    private val integrasjonerClient: IntegrasjonerClient,
    private val søknadService: SøknadService,
    private val ettersendingService: EttersendingService,
    private val opprettOppgaveMapper: OpprettOppgaveMapper,
    private val mappeService: MappeService,
    private val featureToggleService: FeatureToggleService,
) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    fun lagJournalføringsoppgaveForSøknadId(søknadId: String): Long? {
        val søknad: Søknad = søknadService.get(søknadId)
        val journalpostId: String = søknad.journalpostId ?: error("Søknad mangler journalpostId")
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        val prioritet = if (featureToggleService.isEnabled("familie.ef.mottak.prioritet-sommertid")) {
            UtledPrioritetForSøknadUtil.utledPrioritet(søknad)
        } else {
            OppgavePrioritet.NORM
        }
        return lagJournalføringsoppgave(journalpost, prioritet)
    }

    fun lagJournalføringsoppgaveForEttersendingId(ettersendingId: String): Long? {
        val ettersending: Ettersending = ettersendingService.hentEttersending(ettersendingId)
        val journalpostId: String = ettersending.journalpostId ?: error("Ettersending mangler journalpostId")
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        return lagJournalføringsoppgave(journalpost)
    }

    fun lagJournalføringsoppgaveForJournalpostId(journalpostId: String): Long? {
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        try {
            log.info("journalPost=$journalpostId")
            return lagJournalføringsoppgave(journalpost)
        } catch (e: Exception) {
            secureLogger.warn("Kunne ikke opprette journalføringsoppgave for journalpost=$journalpost", e)
            throw e
        }
    }

    fun lagJournalføringsoppgave(journalpost: Journalpost, prioritet: OppgavePrioritet = OppgavePrioritet.NORM): Long? {
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
                    val opprettOppgave =
                        opprettOppgaveMapper.toJournalføringsoppgave(
                            journalpost,
                            BehandlesAvApplikasjon.EF_SAK,
                            finnBehandlendeEnhet(journalpost),
                            prioritet,
                        )
                    return opprettOppgave(opprettOppgave, journalpost)
                }
            }
        } else {
            val error = IllegalStateException(
                "Journalpost ${journalpost.journalpostId} har endret status " +
                    "fra MOTTATT til ${journalpost.journalstatus.name}",
            )
            log.info("OpprettJournalføringOppgaveTask feilet.", error)
            throw error
        }
    }

    private fun finnBehandlendeEnhet(journalpost: Journalpost): String? {
        return finnPersonIdent(journalpost)?.let {
            integrasjonerClient.finnBehandlendeEnhetForPersonMedRelasjoner(it).firstOrNull()?.enhetId
        }
    }

    private fun finnPersonIdent(journalpost: Journalpost): String? {
        return journalpost.bruker?.let {
            when (it.type) {
                BrukerIdType.FNR -> it.id
                BrukerIdType.AKTOERID -> integrasjonerClient.hentIdentForAktørId(it.id)
                BrukerIdType.ORGNR -> error("Kan ikke hente journalpost=${journalpost.journalpostId} for orgnr")
            }
        }
    }

    private fun opprettOppgave(
        opprettOppgave: OpprettOppgaveRequest,
        journalpost: Journalpost,
    ): Long {
        return try {
            val nyOppgave = integrasjonerClient.lagOppgave(opprettOppgave)
            log.info(
                "Oppretter ny ${opprettOppgave.oppgavetype} med oppgaveId=${nyOppgave.oppgaveId} for " +
                    "journalpost journalpostId=${journalpost.journalpostId}",
            )
            nyOppgave.oppgaveId
        } catch (ressursException: RessursException) {
            if (finnerIngenGyldigArbeidsfordelingsenhetForBruker(ressursException)) {
                opprettOppgaveMedEnhetNAY(opprettOppgave, journalpost)
            } else {
                throw ressursException
            }
        }
    }

    private fun opprettOppgaveMedEnhetNAY(
        opprettOppgave: OpprettOppgaveRequest,
        journalpost: Journalpost,
    ): Long {
        val nyOppgave = integrasjonerClient.lagOppgave(opprettOppgave.copy(enhetsnummer = ENHETSNUMMER_NAY))
        log.info(
            "Oppretter ny ${opprettOppgave.oppgavetype} med oppgaveId=${nyOppgave.oppgaveId} for " +
                "journalpost journalpostId=${journalpost.journalpostId} med enhetsnummer=$ENHETSNUMMER_NAY",
        )
        return nyOppgave.oppgaveId
    }

    private fun finnerIngenGyldigArbeidsfordelingsenhetForBruker(ressursException: RessursException): Boolean {
        secureLogger.warn("Feil ved oppretting av oppgave ${ressursException.message}")
        return ressursException.ressurs.melding.contains("Fant ingen gyldig arbeidsfordeling for oppgaven")
    }

    private fun loggSkipOpprettOppgave(journalpostId: String, oppgavetype: Oppgavetype) {
        log.info(
            "Skipper oppretting av journalførings-oppgave. " +
                "Fant åpen oppgave av type $oppgavetype for " +
                "journalpostId=$journalpostId",
        )
    }

    private fun fordelingsoppgaveFinnes(journalpost: Journalpost) =
        integrasjonerClient.finnOppgaver(journalpost.journalpostId, Oppgavetype.Fordeling).antallTreffTotalt > 0L

    private fun journalføringsoppgaveFinnes(journalpost: Journalpost) =
        integrasjonerClient.finnOppgaver(journalpost.journalpostId, Oppgavetype.Journalføring).antallTreffTotalt > 0L

    private fun behandlesakOppgaveFinnes(journalpost: Journalpost) =
        integrasjonerClient.finnOppgaver(journalpost.journalpostId, Oppgavetype.BehandleSak).antallTreffTotalt > 0L

    fun oppdaterOppgaveMedRiktigMappeId(oppgaveId: Long, søknadId: String) {
        val oppgave = integrasjonerClient.hentOppgave(oppgaveId)
        if (kanFlyttesTilMappe(oppgave)) {
            val mappeId = mappeService.finnMappeIdForSøknadOgEnhet(søknadId, oppgave.tildeltEnhetsnr)
            if (mappeId != null) {
                integrasjonerClient.oppdaterOppgave(oppgaveId, oppgave.copy(mappeId = mappeId))
            }
        }
    }

    private fun kanFlyttesTilMappe(oppgave: Oppgave): Boolean =
        kanOppgaveFlyttesTilMappe(oppgave) && kanBehandlesINyLøsning(oppgave)

    private fun kanOppgaveFlyttesTilMappe(oppgave: Oppgave) = oppgave.status != StatusEnum.FEILREGISTRERT &&
        oppgave.status != StatusEnum.FERDIGSTILT &&
        oppgave.mappeId == null &&
        oppgave.tildeltEnhetsnr == ENHETSNUMMER_NAY

    private fun kanBehandlesINyLøsning(oppgave: Oppgave): Boolean =
        when (oppgave.behandlingstema) {
            Behandlingstema.Overgangsstønad.value -> true
            Behandlingstema.Barnetilsyn.value ->
                oppgave.behandlesAvApplikasjon == BehandlesAvApplikasjon.EF_SAK.applikasjon
            Behandlingstema.Skolepenger.value -> true
            null -> false
            else -> error("Kan ikke utlede stønadstype for behangdlingstema ${oppgave.behandlingstema} for oppgave ${oppgave.id}")
        }

    companion object {

        const val ENHETSNUMMER_NAY: String = "4489"
    }
}
