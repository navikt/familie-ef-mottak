package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.infotrygd.Saktreff
import no.nav.familie.kontrakter.ef.infotrygd.Vedtakstreff
import no.nav.familie.kontrakter.ef.sak.DokumentBrevkode
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakRequest
import no.nav.familie.kontrakter.felles.journalpost.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

const val INFOTRYGD = "IT01"
const val EF_SAK = "EF"
const val FAGOMRÅDE_ENSLIG_FORSØRGER = "ENF"
const val SAKSTYPE_SØKNAD = "S"
const val BEHANDLINGSTEMA_OVERGANGSSTØNAD = "ab0071" // https://confluence.adeo.no/display/BOA/Behandlingstema
const val BEHANDLINGSTEMA_BARNETILSYN = "ab0028" // https://confluence.adeo.no/display/BOA/Behandlingstema
const val BEHANDLINGSTEMA_SKOLEPENGER = "ab0177" // https://confluence.adeo.no/display/BOA/Behandlingstema
const val STØNADSKLASSIFISERING_OVERGANGSSTØNAD = "OG"
const val STØNADSKLASSIFISERING_BARNETILSYN = "BT"
const val STØNADSKLASSIFISERING_SKOLEPENGER = "UT"

val stønadsklassifiseringMap =
        mapOf(DOKUMENTTYPE_OVERGANGSSTØNAD to STØNADSKLASSIFISERING_OVERGANGSSTØNAD,
              DOKUMENTTYPE_BARNETILSYN to STØNADSKLASSIFISERING_BARNETILSYN,
              DOKUMENTTYPE_SKOLEPENGER to STØNADSKLASSIFISERING_SKOLEPENGER)

@Service
class SakService(private val integrasjonerClient: IntegrasjonerClient,
                 private val infotrygdService: InfotrygdService,
                 private val søknadService: SøknadService) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun opprettSak(søknadId: String, oppgaveId: String): String? {
        val soknad = søknadService.get(søknadId)
        return if (kanOppretteInfotrygdSak(soknad)) {
            val opprettInfotrygdSakRequest = lagOpprettInfotrygdSakRequest(soknad, oppgaveId)
            val opprettInfotrygdSakResponse = integrasjonerClient.opprettInfotrygdsak(opprettInfotrygdSakRequest)

            logger.info("Infotrygdsak opprettet med saksnummer ${opprettInfotrygdSakResponse.saksId}")
            opprettInfotrygdSakResponse.saksId
        } else {
            null
        }

    }

    fun kanOppretteInfotrygdSak(søknad: Søknad): Boolean {
        val journalposterForBrukerRequest = JournalposterForBrukerRequest(Bruker(søknad.fnr, BrukerIdType.FNR),
                                                                          50,
                                                                          listOf(Tema.ENF),
                                                                          listOf(Journalposttype.I))
        val journalposter = integrasjonerClient.finnJournalposter(journalposterForBrukerRequest)

        val fagsakFinnesForStønad = journalposter.any {
            it.sak?.fagsaksystem in listOf(INFOTRYGD, EF_SAK)
            && it.sak?.fagsakId != null
            && gjelderStønad(søknad, it)
        }
        loggKanOppretteInfotrygdSak(søknad.fnr, fagsakFinnesForStønad, søknad.dokumenttype)
        val erTilknyttetEnhet = integrasjonerClient.finnBehandlendeEnhet(søknad.fnr).isNotEmpty()

        return !fagsakFinnesForStønad && erTilknyttetEnhet
    }

    private fun loggKanOppretteInfotrygdSak(personIdent: String, fagsakFinnesForStønad: Boolean, dokumenttype: String) {
        val stønadType = dokumenttypeTilStønadType(dokumenttype) ?: return
        try {
            val innslagHosInfotrygd = infotrygdService.hentInslagHosInfotrygd(personIdent)
            val vedtak = innslagHosInfotrygd.vedtak.filter { it.stønadType == stønadType }
            val saker = innslagHosInfotrygd.saker.filter { it.stønadType == stønadType }
            val vedtakFinnes = vedtak.isNotEmpty()
            val sakerFinnes = saker.isNotEmpty()
            val kanOppretteInfotrygdSakLog = "kanOppretteInfotrygdSak -" +
                                             " stønadType=$stønadType" +
                                             " fagsakFinnesForStønad=$fagsakFinnesForStønad"
            logger.info("$kanOppretteInfotrygdSakLog vedtakFinnes=$vedtakFinnes sakerFinnes=$sakerFinnes")

            val finnesAllerede = fagsakFinnesForStønad || vedtakFinnes || sakerFinnes
            if (finnesAllerede) {
                secureLogger.info(kanOppretteInfotrygdSakLog +
                                  " personIdent=$personIdent" +
                                  " vedtak=${vedtak.sortedBy(Vedtakstreff::personIdent)}" +
                                  " saker=${saker.sortedBy(Saktreff::personIdent)}")
            }
        } catch (e: Exception) {
            logger.warn("Feilet sjekk mot infotrygdReplika")
            secureLogger.warn("Feilet sjekk mot infotrygdReplika", e)
        }
    }

    private fun dokumenttypeTilStønadType(dokumenttype: String): StønadType? {
        return when(dokumenttype) {
            DOKUMENTTYPE_OVERGANGSSTØNAD -> StønadType.OVERGANGSSTØNAD
            DOKUMENTTYPE_BARNETILSYN -> StønadType.BARNETILSYN
            DOKUMENTTYPE_SKOLEPENGER -> StønadType.SKOLEPENGER
            else -> null
        }
    }

    private fun gjelderStønad(søknad: Søknad, journalpost: Journalpost): Boolean {
        return when (søknad.dokumenttype) {
            DOKUMENTTYPE_OVERGANGSSTØNAD -> harBrevkode(journalpost, DokumentBrevkode.OVERGANGSSTØNAD)
            DOKUMENTTYPE_BARNETILSYN -> harBrevkode(journalpost, DokumentBrevkode.BARNETILSYN)
            DOKUMENTTYPE_SKOLEPENGER -> harBrevkode(journalpost, DokumentBrevkode.SKOLEPENGER)
            else -> false
        }
    }

    private fun harBrevkode(journalpost: Journalpost, dokumentBrevkode: DokumentBrevkode): Boolean {
        return journalpost.dokumenter
                       ?.filter { dokument -> DokumentBrevkode.erGyldigBrevkode(dokument.brevkode) }
                       ?.filter { DokumentBrevkode.fraBrevkode(it.brevkode) == dokumentBrevkode }
                       ?.any {
                           logger.info("Fant riktig brevkode=$dokumentBrevkode for journalpost=${journalpost.journalpostId} og dokument=${it.dokumentInfoId}")
                           return true
                       } ?: false

    }


    private fun lagOpprettInfotrygdSakRequest(søknad: Søknad, oppgaveId: String): OpprettInfotrygdSakRequest {
        val stønadsklassifisering = stønadsklassifiseringMap[søknad.dokumenttype]
        val enheter = integrasjonerClient.finnBehandlendeEnhet(søknad.fnr)
        if (enheter.size > 1) {
            logger.warn("Fant mer enn 1 enhet for ${søknad.id}: $enheter")
        }

        val mottagendeEnhet = enheter.firstOrNull()?.enhetId
                              ?: error("Ingen behandlende enhet funnet for søknad ${søknad.id} ")

        return OpprettInfotrygdSakRequest(fnr = søknad.fnr,
                                          fagomrade = FAGOMRÅDE_ENSLIG_FORSØRGER,
                                          stonadsklassifisering2 = stønadsklassifisering,
                                          type = SAKSTYPE_SØKNAD,
                                          opprettetAvOrganisasjonsEnhetsId = mottagendeEnhet,
                                          mottakerOrganisasjonsEnhetsId = mottagendeEnhet,
                                          mottattdato = søknad.opprettetTid.toLocalDate(),
                                          sendBekreftelsesbrev = false,
                                          oppgaveId = oppgaveId)
    }
}
