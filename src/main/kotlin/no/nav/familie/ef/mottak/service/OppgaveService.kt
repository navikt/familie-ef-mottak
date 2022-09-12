package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.BehandlesAvApplikasjon
import no.nav.familie.ef.mottak.mapper.OpprettOppgaveMapper
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.ef.søknad.Aktivitet
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
import no.nav.familie.kontrakter.felles.Behandlingstema
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
    private val featureToggleService: FeatureToggleService
) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    fun lagJournalføringsoppgaveForSøknadId(søknadId: String): Long? {
        val søknad: Søknad = søknadService.get(søknadId)
        val journalpostId: String = søknad.journalpostId ?: error("Søknad mangler journalpostId")
        val journalpost = integrasjonerClient.hentJournalpost(journalpostId)
        return lagJournalføringsoppgave(journalpost)
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
                    val opprettOppgave =
                        opprettOppgaveMapper.toJournalføringsoppgave(
                            journalpost,
                            BehandlesAvApplikasjon.EF_SAK,
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

            val mappe = finnMappe(mapperResponse, finnSøkestreng(oppgave, søknadId))
            integrasjonerClient.oppdaterOppgave(oppgaveId, oppgave.copy(mappeId = mappe.id.toLong()))
        } else {
            secureLogger.info("Flytter ikke oppgave til mappe $oppgave")
        }
    }

    private fun erSkolepenger(oppgave: Oppgave) =
        oppgave.behandlingstema == Behandlingstema.Skolepenger.value && oppgave.tildeltEnhetsnr == ENHETSNUMMER_NAY

    private fun erSelvstendig(aktivitet: Søknadsfelt<Aktivitet>) =
        aktivitet.verdi.firmaer?.verdi?.isNotEmpty() ?: false ||
            aktivitet.verdi.virksomhet?.verdi != null

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
            Behandlingstema.Overgangsstønad.value -> true
            Behandlingstema.Barnetilsyn.value ->
                oppgave.behandlesAvApplikasjon == BehandlesAvApplikasjon.EF_SAK.applikasjon
            Behandlingstema.Skolepenger.value -> true
            null -> false
            else -> error("Kan ikke utlede stønadstype for behangdlingstema ${oppgave.behandlingstema} for oppgave ${oppgave.id}")
        }

    private fun finnSøkestreng(
        oppgave: Oppgave,
        søknadId: String?
    ): String {
        return if (erSkolepenger(oppgave)) {
            MappeSøkestreng.UPLASSERT.søkestreng
        } else {
            mappeFraSøknad(søknadId, oppgave) ?: MappeSøkestreng.UPLASSERT.søkestreng
        }
    }

    private fun mappeFraSøknad(søknadId: String?, oppgave: Oppgave): String? {
        val toggleEnabled =
            featureToggleService.isEnabled("familie.ef.mottak.mappe.selvstendig.tilsynskrevende")
        if (søknadId == null || !toggleEnabled) return null

        return when (oppgave.behandlingstema) {
            Behandlingstema.Overgangsstønad.value -> mappeFraOvergangsstønad(søknadId)
            Behandlingstema.Barnetilsyn.value -> mappeFraBarnetilsyn(søknadId)
            else -> null
        }
    }

    private fun mappeFraBarnetilsyn(søknadId: String): String? {
        val søknadJson = søknadService.getOrNull(søknadId)?.søknadJson
        if (søknadJson == null) {
            log.warn("Finner ikke søknad - antar dette er en ettersending $søknadId")
            return null
        }
        val søknad = objectMapper.readValue<SøknadBarnetilsyn>(søknadJson.data)
        return if (søknad.barn.verdi.any { it.barnepass?.verdi?.årsakBarnepass?.svarId == "trengerMerPassEnnJevnaldrede" }) {
            MappeSøkestreng.SÆRLIG_TILSYNSKREVENDE.søkestreng
        } else if (erSelvstendig(søknad.aktivitet)) {
            MappeSøkestreng.SELVSTENDIG.søkestreng
        } else {
            null
        }
    }

    private fun mappeFraOvergangsstønad(søknadId: String): String? {
        val søknadJson = søknadService.getOrNull(søknadId)?.søknadJson
        if (søknadJson == null) {
            log.warn("Finner ikke søknad - antar dette er en ettersending $søknadId")
            return null
        }
        val søknad = objectMapper.readValue<SøknadOvergangsstønad>(søknadJson.data)
        return if (søknad.situasjon.verdi.barnMedSærligeBehov?.verdi != null) {
            MappeSøkestreng.SÆRLIG_TILSYNSKREVENDE.søkestreng
        } else if (erSelvstendig(søknad.aktivitet)) {
            MappeSøkestreng.SELVSTENDIG.søkestreng
        } else {
            null
        }
    }

    private fun finnMappe(mapperResponse: FinnMappeResponseDto, søkestreng: String): MappeDto {
        return mapperResponse.mapper.filter {
            it.navn.contains("EF Sak", true) &&
                it.navn.contains(søkestreng, true)
        }.maxByOrNull { it.id }
            ?: error("Fant ikke mappe for $søkestreng")
    }

    companion object {

        private const val ENHETSNUMMER_NAY: String = "4489"
        private const val ENHETSNUMMER_EGEN_ANSATT: String = "4483"
    }
}

enum class MappeSøkestreng(val søkestreng: String) {
    SÆRLIG_TILSYNSKREVENDE("60 Særlig tilsynskrevende"),
    SELVSTENDIG("61 Selvstendig næringsdrivende"),
    UPLASSERT("01 Uplassert")
}
