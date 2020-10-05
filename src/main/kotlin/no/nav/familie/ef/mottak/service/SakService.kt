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

const val FAGOMRÅDE_ENSLIG_FORSØRGER = "EF"

const val SAKSTYPE_SØKNAD = "S"


@Service
class SakService(private val integrasjonerClient: IntegrasjonerClient,
                 private val søknadService: SøknadService) {

    private val logger = LoggerFactory.getLogger(this::class.java)


    fun opprettSakOmIngenFinnes(søknadId: String) {

        val soknad = søknadService.get(søknadId)


        val journalposterForBrukerRequest = JournalposterForBrukerRequest(Bruker(soknad.fnr, BrukerIdType.FNR),
                                                                          50,
                                                                          listOf(Tema.ENF),
                                                                          listOf(Journalposttype.I))
        val journalposter = integrasjonerClient.finnJournalposter(journalposterForBrukerRequest)

        val fagsakOpprettet = journalposter.any { it.sak?.fagsaksystem == INFOTRYGD && it.sak?.fagsakId != null }

        if (fagsakOpprettet) {
            return
        }

        val opprettInfotrygdSakRequest = lagOpprettInfotrygdSakRequest(soknad)


        val opprettInfotrygdSakResponse =
                integrasjonerClient.opprettInfotrygdsak(opprettInfotrygdSakRequest)

        logger.info("Infotrygdsak opprettet", opprettInfotrygdSakResponse)

    }

    private fun lagOpprettInfotrygdSakRequest(soknad: Soknad): OpprettInfotrygdSakRequest {
        val finnOppgaver =
                integrasjonerClient.finnOppgaver(soknad.journalpostId!!, Oppgavetype.Journalføring)

        val oppgave = finnOppgaver.oppgaver.first()

        val stønadsklassifisering = when (soknad.dokumenttype) {
            DOKUMENTTYPE_OVERGANGSSTØNAD -> "OG"
            DOKUMENTTYPE_BARNETILSYN -> "BT"
            DOKUMENTTYPE_SKOLEPENGER -> "UT"
            else -> error("Ukjent dokumenttype")
        }

        val enhet = integrasjonerClient.finnBehandlendeEnhet(soknad.fnr)


        val opprettInfotrygdSakRequest =
                OpprettInfotrygdSakRequest(fnr = soknad.fnr,
                                           fagomrade = FAGOMRÅDE_ENSLIG_FORSØRGER,
                                           stonadsklassifisering2 = stønadsklassifisering,
                                           stonadsklassifisering3 = null,
                                           type = SAKSTYPE_SØKNAD,
                                           opprettetAv = "MOTTAK",
                                           opprettetAvOrganisasjonsEnhetsId = null,
                                           mottakerOrganisasjonsEnhetsId = enhet.firstOrNull()?.enhetId,
                                           motattdato = soknad.opprettetTid.toLocalDate(),
                                           sendBekreftelsesbrev = false,
                                           oppgaveId = oppgave.id?.toString(),
                                           oppgaveOrganisasjonsenhetId = oppgave.opprettetAvEnhetsnr)
        return opprettInfotrygdSakRequest
    }
}
