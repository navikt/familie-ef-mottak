package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.ef.mottak.mapper.BehandlesAvApplikasjon
import no.nav.familie.ef.mottak.mapper.BehandlesAvApplikasjon.EF_SAK_BLANKETT
import no.nav.familie.ef.mottak.mapper.BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
import no.nav.familie.ef.mottak.mapper.BehandlesAvApplikasjon.UAVKLART
import no.nav.familie.ef.mottak.mapper.OpprettOppgaveMapper
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.util.dokumenttypeTilStønadType
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.StatusEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OppgaveService(
    private val integrasjonerClient: IntegrasjonerClient,
    private val featureToggleService: FeatureToggleService,
    private val søknadService: SøknadService,
    private val ettersendingService: EttersendingService,
    private val opprettOppgaveMapper: OpprettOppgaveMapper,
    private val sakService: SakService,
    private val saksbehandlingClient: SaksbehandlingClient
) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    fun lagJournalføringsoppgaveForSøknadId(søknadId: String): Long? {
        val søknad: Søknad = søknadService.get(søknadId)
        val journalpostId: String = søknad.journalpostId ?: error("Søknad mangler journalpostId")
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        val behandlesAvApplikasjon = utledBehandlesAvApplikasjon(søknad)
        return lagJournalføringsoppgave(journalpost, behandlesAvApplikasjon)
    }

    fun lagJournalføringsoppgaveForEttersendingId(ettersendingId: String): Long? {
        val ettersending: Ettersending = ettersendingService.hentEttersending(ettersendingId)
        val journalpostId: String = ettersending.journalpostId ?: error("Ettersending mangler journalpostId")
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        val stønadType = StønadType.valueOf(ettersending.stønadType)
        val behandlesAvApplikasjon = utledBehandlesAvApplikasjonForEttersending(fnr = ettersending.fnr, stønadType = stønadType)
        return lagJournalføringsoppgave(journalpost, behandlesAvApplikasjon)
    }

    /**
     * Då vi ikke er sikre på at stønadstypen er riktig eller eksisterer på oppgaven så sjekker vi om den finnes i ny løsning
     * Hvis den finnes setter vi att den må sjekkes opp før man behandler den
     */
    fun lagJournalføringsoppgaveForJournalpostId(journalpostId: String): Long? {
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        val finnesBehandlingForPerson = finnesBehandlingForPerson(journalpost)
        try {
            log.info("journalPost=$journalpostId finnesBehandlingForPerson=$finnesBehandlingForPerson")
            val behandlesAvApplikasjon =
                if (finnesBehandlingForPerson) UAVKLART else BehandlesAvApplikasjon.INFOTRYGD
            return lagJournalføringsoppgave(journalpost, behandlesAvApplikasjon)
        } catch (e: Exception) {
            secureLogger.warn("Kunne ikke opprette journalføringsoppgave for journalpost=$journalpost", e)
            throw e
        }
    }

    private fun finnesBehandlingForPerson(journalpost: Journalpost): Boolean {
        val personIdent = finnPersonIdent(journalpost) ?: return false
        return saksbehandlingClient.finnesBehandlingForPerson(personIdent)
    }

    fun lagBehandleSakOppgave(journalpost: Journalpost, behandlesAvApplikasjon: BehandlesAvApplikasjon): Long {
        val opprettOppgave = opprettOppgaveMapper.toBehandleSakOppgave(journalpost, behandlesAvApplikasjon, finnBehandlendeEnhet(journalpost))
        return opprettOppgave(opprettOppgave, journalpost)
    }

    fun settSaksnummerPåInfotrygdOppgave(
        oppgaveId: Long,
        saksblokk: String,
        saksnummer: String
    ): Long {
        val oppgave: Oppgave = integrasjonerClient.hentOppgave(oppgaveId)
        val oppdatertOppgave = oppgave.copy(
            saksreferanse = saksnummer,
            beskrivelse = "${oppgave.beskrivelse} - Saksblokk: $saksblokk, Saksnummer: $saksnummer [Automatisk journalført]",
            behandlesAvApplikasjon = EF_SAK_BLANKETT.applikasjon,
        )
        return integrasjonerClient.oppdaterOppgave(oppgaveId, oppdatertOppgave)
    }

    fun lagJournalføringsoppgave(
        journalpost: Journalpost,
        behandlesAvApplikasjon: BehandlesAvApplikasjon
    ): Long? {

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
                        opprettOppgaveMapper.toJournalføringsoppgave(journalpost, behandlesAvApplikasjon, finnBehandlendeEnhet(journalpost))
                    return opprettOppgave(opprettOppgave, journalpost)
                }
            }
        } else {
            val error = IllegalStateException(
                "Journalpost ${journalpost.journalpostId} har endret status " +
                    "fra MOTTATT til ${journalpost.journalstatus.name}"
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
        journalpost: Journalpost
    ): Long {

        return try {
            val nyOppgave = integrasjonerClient.lagOppgave(opprettOppgave)
            log.info(
                "Oppretter ny ${opprettOppgave.oppgavetype} med oppgaveId=${nyOppgave.oppgaveId} for " +
                    "journalpost journalpostId=${journalpost.journalpostId}"
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
        journalpost: Journalpost
    ): Long {
        val nyOppgave = integrasjonerClient.lagOppgave(opprettOppgave.copy(enhetsnummer = ENHETSNUMMER_NAY))
        log.info(
            "Oppretter ny ${opprettOppgave.oppgavetype} med oppgaveId=${nyOppgave.oppgaveId} for " +
                "journalpost journalpostId=${journalpost.journalpostId} med enhetsnummer=$ENHETSNUMMER_NAY"
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
                "journalpostId=$journalpostId"
        )
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

    private fun utledBehandlesAvApplikasjon(søknad: Søknad): BehandlesAvApplikasjon {
        log.info("utledBehandlesAvApplikasjon dokumenttype=${søknad.dokumenttype}")
        val stønadType = dokumenttypeTilStønadType(søknad.dokumenttype) ?: return BehandlesAvApplikasjon.INFOTRYGD
        return if (finnesBehandlingINyLøsning(søknad.fnr, stønadType)) {
            BehandlesAvApplikasjon.EF_SAK
        } else {
            when (stønadType) {
                StønadType.OVERGANGSSTØNAD ->
                    if (sakService.finnesIkkeIInfotrygd(søknad)) EF_SAK_INFOTRYGD else BehandlesAvApplikasjon.INFOTRYGD
                StønadType.BARNETILSYN, StønadType.SKOLEPENGER ->
                    if (sakService.finnesIkkeÅpenSakIInfotrygd(søknad.fnr, stønadType)) EF_SAK_INFOTRYGD else BehandlesAvApplikasjon.INFOTRYGD
            }
        }
    }

    private fun utledBehandlesAvApplikasjonForEttersending(fnr: String, stønadType: StønadType): BehandlesAvApplikasjon {
        log.info("utledBehandlesAvApplikasjon stønadType=$stønadType")
        return if (finnesBehandlingINyLøsning(fnr, stønadType)) {
            BehandlesAvApplikasjon.EF_SAK
        } else if (sakService.finnesIkkeIInfotrygd(fnr, stønadType) || stønadType != StønadType.OVERGANGSSTØNAD) {
            EF_SAK_INFOTRYGD
        } else {
            BehandlesAvApplikasjon.INFOTRYGD
        }
    }

    private fun finnesBehandlingINyLøsning(
        fnr: String,
        stønadType: StønadType
    ): Boolean {
        val finnesBehandlingForPerson = saksbehandlingClient.finnesBehandlingForPerson(fnr, stønadType)
        log.info("Sjekk om behandling finnes i ny løsning for personen - finnesBehandlingForPerson=$finnesBehandlingForPerson")
        return finnesBehandlingForPerson
    }

    fun oppdaterOppgaveMedRiktigMappeId(oppgaveId: Long) {
        val oppgave = integrasjonerClient.hentOppgave(oppgaveId)

        if (skalFlyttesTilMappe(oppgave)) {
            val finnMappeRequest = FinnMappeRequest(
                tema = listOf(),
                enhetsnr = oppgave.tildeltEnhetsnr
                    ?: error("Oppgave mangler tildelt enhetsnummer"),
                opprettetFom = null,
                limit = 1000
            )
            val mapperResponse = integrasjonerClient.finnMappe(finnMappeRequest)

            log.info("Mapper funnet: Antall: ${mapperResponse.antallTreffTotalt}, ${mapperResponse.mapper} ")

            val mappe =
                if (oppgave.behandlingstema == BEHANDLINGSTEMA_SKOLEPENGER && oppgave.tildeltEnhetsnr == ENHETSNUMMER_NAY) {
                    hentOpplæringsmappeSkolepenger(mapperResponse)
                } else {
                    hentMappeEfSakUpplassert(mapperResponse)
                }

            integrasjonerClient.oppdaterOppgave(oppgaveId, oppgave.copy(mappeId = mappe.id.toLong()))
        } else {
            secureLogger.info("Flytter ikke oppgave til mappe $oppgave")
        }
    }

    private fun hentMappeEfSakUpplassert(mapperResponse: FinnMappeResponseDto) =
        (
            mapperResponse.mapper.find { it.navn.contains("EF Sak", true) && it.navn.contains("01") }
                ?: error("Fant ikke mappe for uplassert oppgave (EF Sak og 01)")
            )

    private fun hentOpplæringsmappeSkolepenger(
        mapperResponse: FinnMappeResponseDto,
    ) = (
        mapperResponse.mapper.find {
            it.navn.contains("EF Sak", true) &&
                it.navn.contains("65 Opplæring", true)
        }
            ?: error("Fant ikke mappe EF Sak - 65 Opplæring, for plassering av skolepengeroppgave")
        )

    private fun skalFlyttesTilMappe(oppgave: Oppgave): Boolean =
        kanOppgaveFlyttesTilMappe(oppgave) && kanBehandlesINyLøsning(oppgave)

    private fun kanOppgaveFlyttesTilMappe(oppgave: Oppgave) = oppgave.status != StatusEnum.FEILREGISTRERT &&
        oppgave.status != StatusEnum.FERDIGSTILT &&
        oppgave.mappeId == null &&
        (
            oppgave.tildeltEnhetsnr == ENHETSNUMMER_NAY ||
                oppgave.tildeltEnhetsnr == ENHETSNUMMER_EGEN_ANSATT
            )

    private fun kanBehandlesINyLøsning(oppgave: Oppgave): Boolean =
        when (oppgave.behandlingstema) {
            BEHANDLINGSTEMA_OVERGANGSSTØNAD -> true
            BEHANDLINGSTEMA_BARNETILSYN ->
                oppgave.behandlesAvApplikasjon == BehandlesAvApplikasjon.EF_SAK.applikasjon ||
                    oppgave.behandlesAvApplikasjon == EF_SAK_INFOTRYGD.applikasjon
            BEHANDLINGSTEMA_SKOLEPENGER -> true
            null -> false
            else -> error("Kan ikke utlede stønadstype for behangdlingstema ${oppgave.behandlingstema} for oppgave ${oppgave.id}")
        }
    companion object {

        private const val ENHETSNUMMER_NAY: String = "4489"
        private const val ENHETSNUMMER_EGEN_ANSATT: String = "4483"
    }
}
