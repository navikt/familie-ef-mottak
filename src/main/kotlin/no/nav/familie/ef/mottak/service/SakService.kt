package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakRequest
import no.nav.familie.kontrakter.felles.journalpost.*
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
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
val behandlingstemaMap =
        mapOf(DOKUMENTTYPE_OVERGANGSSTØNAD to BEHANDLINGSTEMA_OVERGANGSSTØNAD,
              DOKUMENTTYPE_BARNETILSYN to BEHANDLINGSTEMA_BARNETILSYN,
              DOKUMENTTYPE_SKOLEPENGER to BEHANDLINGSTEMA_SKOLEPENGER)


@Service
class SakService(private val integrasjonerClient: IntegrasjonerClient,
                 private val søknadService: SøknadService) {

    private val logger = LoggerFactory.getLogger(this::class.java)


    fun opprettSakOmIngenFinnes(søknadId: String): String? {

        val soknad = søknadService.get(søknadId)

        val journalposterForBrukerRequest = JournalposterForBrukerRequest(Bruker(soknad.fnr, BrukerIdType.FNR),
                                                                          50,
                                                                          listOf(Tema.ENF),
                                                                          listOf(Journalposttype.I))
        val journalposter = integrasjonerClient.finnJournalposter(journalposterForBrukerRequest)

        val fagsakOpprettet = journalposter.any {
            it.sak?.fagsaksystem in listOf(INFOTRYGD, EF_SAK)
            && it.sak?.fagsakId != null
            && it.behandlingstema == behandlingstemaMap[soknad.dokumenttype]
        }

        if (fagsakOpprettet) {
            return null
        }

        val opprettInfotrygdSakRequest = lagOpprettInfotrygdSakRequest(soknad)

        val opprettInfotrygdSakResponse =
                integrasjonerClient.opprettInfotrygdsak(opprettInfotrygdSakRequest)

        logger.info("Infotrygdsak opprettet med saksnummer ", opprettInfotrygdSakResponse.saksId)
        return opprettInfotrygdSakResponse.saksId
    }

    private fun lagOpprettInfotrygdSakRequest(soknad: Soknad): OpprettInfotrygdSakRequest {
        val finnOppgaver =
                integrasjonerClient.finnOppgaver(soknad.journalpostId!!, Oppgavetype.Journalføring)

        val oppgave = finnOppgaver.oppgaver.first()
        val stønadsklassifisering = stønadsklassifiseringMap[soknad.dokumenttype]
        val enhet = integrasjonerClient.finnBehandlendeEnhet(soknad.fnr)
        val mottagendeEnhet = enhet.firstOrNull()?.enhetId
                              ?: error("Ingen behandlende enhet funnet for søknad ${soknad.id} ")

        return OpprettInfotrygdSakRequest(fnr = soknad.fnr,
                                  fagomrade = FAGOMRÅDE_ENSLIG_FORSØRGER,
                                  stonadsklassifisering2 = stønadsklassifisering,
                                  type = SAKSTYPE_SØKNAD,
                                  opprettetAvOrganisasjonsEnhetsId = mottagendeEnhet,
                                  mottakerOrganisasjonsEnhetsId = mottagendeEnhet,
                                  mottattdato = soknad.opprettetTid.toLocalDate(),
                                  sendBekreftelsesbrev = false,
                                  oppgaveId = oppgave.id?.toString(),
                                  oppgaveOrganisasjonsenhetId = oppgave.opprettetAvEnhetsnr)
    }
}
