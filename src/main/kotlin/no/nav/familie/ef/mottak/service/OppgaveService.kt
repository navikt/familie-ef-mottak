package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.BehandlesAvApplikasjon
import no.nav.familie.ef.mottak.mapper.BehandlesAvApplikasjon.EF_SAK_BLANKETT
import no.nav.familie.ef.mottak.mapper.BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
import no.nav.familie.ef.mottak.mapper.OpprettOppgaveMapper
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.ef.søknad.Aktivitet
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.MappeDto
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
    private val søknadService: SøknadService,
    private val ettersendingService: EttersendingService,
    private val opprettOppgaveMapper: OpprettOppgaveMapper,
    private val featureToggleService: FeatureToggleService,
) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    fun lagJournalføringsoppgaveForSøknadId(søknadId: String): Long? {
        val søknad: Søknad = søknadService.get(søknadId)
        val journalpostId: String = søknad.journalpostId ?: error("Søknad mangler journalpostId")
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        return lagJournalføringsoppgave(journalpost, BehandlesAvApplikasjon.EF_SAK)
    }

    fun lagJournalføringsoppgaveForEttersendingId(ettersendingId: String): Long? {
        val ettersending: Ettersending = ettersendingService.hentEttersending(ettersendingId)
        val journalpostId: String = ettersending.journalpostId ?: error("Ettersending mangler journalpostId")
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        return lagJournalføringsoppgave(journalpost, BehandlesAvApplikasjon.EF_SAK)
    }

    /**
     * Då vi ikke er sikre på at stønadstypen er riktig eller eksisterer på oppgaven så sjekker vi om den finnes i ny løsning
     * Hvis den finnes setter vi att den må sjekkes opp før man behandler den
     */
    fun lagJournalføringsoppgaveForJournalpostId(journalpostId: String): Long? {
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        try {
            log.info("journalPost=$journalpostId")
            return lagJournalføringsoppgave(journalpost, BehandlesAvApplikasjon.UAVKLART)
        } catch (e: Exception) {
            secureLogger.warn("Kunne ikke opprette journalføringsoppgave for journalpost=$journalpost", e)
            throw e
        }
    }

    fun lagBehandleSakOppgave(journalpost: Journalpost, behandlesAvApplikasjon: BehandlesAvApplikasjon): Long {
        val opprettOppgave =
            opprettOppgaveMapper.toBehandleSakOppgave(
                journalpost,
                behandlesAvApplikasjon,
                finnBehandlendeEnhet(journalpost)
            )
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
                        opprettOppgaveMapper.toJournalføringsoppgave(
                            journalpost,
                            behandlesAvApplikasjon,
                            finnBehandlendeEnhet(journalpost)
                        )
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
                val oppgaveId =
                    oppgaver.oppgaver.first().id ?: error("Finner ikke oppgaveId for journalpost=$journalpostId")
                integrasjonerClient.ferdigstillOppgave(oppgaveId)
            }
            else -> {
                val error =
                    IllegalStateException("Fant ${oppgaver.antallTreffTotalt} oppgaver for journalpost=$journalpostId")
                log.warn("Kan ikke ferdigstille oppgave", error)
                throw error
            }
        }
    }

    fun oppdaterOppgaveMedRiktigMappeId(oppgaveId: Long, søknadId: String?) {
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
            val toggleEnabled =
                featureToggleService.isEnabled("familie.ef.mottak.mappe.selvstendig.tilsynskrevende")

            val mappe =
                if (erSkolepenger(oppgave)) {
                    finnMappe(mapperResponse, "65 Opplæring")
                } else if (toggleEnabled && harTilsynskrevendeBarn(søknadId, oppgave)) {
                    finnMappe(mapperResponse, søkestreng = "60 Særlig tilsynskrevende")
                } else if (toggleEnabled && erSelvstendig(søknadId, oppgave)) {
                    finnMappe(mapperResponse, søkestreng = "61 Selvstendig næringsdrivende")
                } else {
                    finnMappe(mapperResponse, "01 Uplassert")
                }
            integrasjonerClient.oppdaterOppgave(oppgaveId, oppgave.copy(mappeId = mappe.id.toLong()))
        } else {
            secureLogger.info("Flytter ikke oppgave til mappe $oppgave")
        }
    }

    private fun erSkolepenger(oppgave: Oppgave) =
        oppgave.behandlingstema == BEHANDLINGSTEMA_SKOLEPENGER && oppgave.tildeltEnhetsnr == ENHETSNUMMER_NAY

    private fun erSelvstendig(søknadId: String?, oppgave: Oppgave) =
        if (søknadId != null && oppgave.behandlingstema == BEHANDLINGSTEMA_OVERGANGSSTØNAD) {
            val søknadJson = søknadService.get(søknadId).søknadJson
            val søknadOvergangsstønad = objectMapper.readValue<SøknadOvergangsstønad>(søknadJson.data)
            erSelvstendig(søknadOvergangsstønad.aktivitet)
        } else if (søknadId != null && oppgave.behandlingstema == BEHANDLINGSTEMA_BARNETILSYN) {
            val søknadJson = søknadService.get(søknadId).søknadJson
            val søknadBarnetilsyn = objectMapper.readValue<SøknadBarnetilsyn>(søknadJson.data)
            erSelvstendig(søknadBarnetilsyn.aktivitet)
        } else {
            false
        }

    private fun erSelvstendig(aktivitet: Søknadsfelt<Aktivitet>) =
        aktivitet.verdi.firmaer?.verdi?.isNotEmpty() ?: false ||
            aktivitet.verdi.virksomhet?.verdi != null

    private fun harTilsynskrevendeBarn(
        søknadId: String?,
        oppgave: Oppgave
    ): Boolean {
        return if (søknadId != null && oppgave.behandlingstema == BEHANDLINGSTEMA_OVERGANGSSTØNAD) {
            val søknadJson = søknadService.get(søknadId).søknadJson
            val søknadOvergangsstønad = objectMapper.readValue<SøknadOvergangsstønad>(søknadJson.data)
            søknadOvergangsstønad.situasjon.verdi.barnMedSærligeBehov?.verdi != null
        } else if (søknadId != null && oppgave.behandlingstema == BEHANDLINGSTEMA_BARNETILSYN) {
            val søknadJson = søknadService.get(søknadId).søknadJson
            val søknadBarnetilsyn = objectMapper.readValue<SøknadBarnetilsyn>(søknadJson.data)
            søknadBarnetilsyn.barn.verdi.any { it.særligeTilsynsbehov != null }
        } else {
            false
        }
    }

    private fun finnMappe(mapperResponse: FinnMappeResponseDto, søkestreng: String): MappeDto {
        return mapperResponse.mapper.filter {
            it.navn.contains("EF Sak", true) &&
                it.navn.contains(søkestreng, true)
        }.maxByOrNull { it.id }
            ?: error("Fant ikke mappe for $søkestreng")
    }

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
