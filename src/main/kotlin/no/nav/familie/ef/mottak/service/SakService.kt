package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.sak.DokumentBrevkode
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
                 private val søknadService: SøknadService) {

    private val logger = LoggerFactory.getLogger(this::class.java)


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

    fun kanOppretteInfotrygdSak(soknad: Soknad): Boolean {
        val journalposterForBrukerRequest = JournalposterForBrukerRequest(Bruker(soknad.fnr, BrukerIdType.FNR),
                                                                          50,
                                                                          listOf(Tema.ENF),
                                                                          listOf(Journalposttype.I))
        val journalposter = integrasjonerClient.finnJournalposter(journalposterForBrukerRequest)

        val fagsakFinnesForStønad = journalposter.any {
            it.sak?.fagsaksystem in listOf(INFOTRYGD, EF_SAK)
            && it.sak?.fagsakId != null
            && gjelderStønad(soknad, it)
        }
        return !fagsakFinnesForStønad
    }

    private fun gjelderStønad(soknad: Soknad, journalpost: Journalpost): Boolean {
        return when (soknad.dokumenttype) {
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
                }?: false

    }


    private fun lagOpprettInfotrygdSakRequest(soknad: Soknad, oppgaveId: String): OpprettInfotrygdSakRequest {
        val stønadsklassifisering = stønadsklassifiseringMap[soknad.dokumenttype]
        val enheter = integrasjonerClient.finnBehandlendeEnhet(soknad.fnr)
        if (enheter.size > 1) {
            logger.warn("Fant mer enn 1 enhet for ${soknad.id}: $enheter")
        }

        val mottagendeEnhet = enheter.firstOrNull()?.enhetId
                              ?: error("Ingen behandlende enhet funnet for søknad ${soknad.id} ")

        return OpprettInfotrygdSakRequest(fnr = soknad.fnr,
                                          fagomrade = FAGOMRÅDE_ENSLIG_FORSØRGER,
                                          stonadsklassifisering2 = stønadsklassifisering,
                                          type = SAKSTYPE_SØKNAD,
                                          opprettetAvOrganisasjonsEnhetsId = mottagendeEnhet,
                                          mottakerOrganisasjonsEnhetsId = mottagendeEnhet,
                                          mottattdato = soknad.opprettetTid.toLocalDate(),
                                          sendBekreftelsesbrev = false,
                                          oppgaveId = oppgaveId)
    }
}
