package no.nav.familie.ef.mottak.service

import io.mockk.*
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakRequest
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakResponse
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.journalpost.Sak
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class SakServiceTest {

    private val integrasjonerClient: IntegrasjonerClient = mockk()

    private val søknadService: SøknadService = mockk()

    private val sakService = SakService(integrasjonerClient,
                                        søknadService)

    @Test
    fun `opprettSakOmIngenFinnes gjør ingenting om sak finnes`() {

        every { søknadService.get("1") } returns Soknad(søknadJson = "", dokumenttype = "", journalpostId = "15", fnr = "123")
        every { integrasjonerClient.finnJournalposter(any()) }
                .returns(listOf(Journalpost(journalpostId = "15",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.FERDIGSTILT,
                                            sak = Sak(fagsakId = "23",
                                                      fagsaksystem = INFOTRYGD))))

        sakService.opprettSakOmIngenFinnes("1")

        verify { integrasjonerClient.opprettInfotrygdsak(any()) wasNot Called }
    }

    @Test
    fun `opprettSakOmIngenFinnes oppretter sak om ingen finnes`() {

        val slot = slot<OpprettInfotrygdSakRequest>()
        every { søknadService.get("1") }
                .returns(Soknad(søknadJson = "",
                                dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                                journalpostId = "15",
                                fnr = "123",
                opprettetTid = LocalDateTime.of(2014, 1, 16, 12, 45)))
        every { integrasjonerClient.finnJournalposter(any()) }
                .returns(listOf(Journalpost(journalpostId = "15",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.FERDIGSTILT)))
        every { integrasjonerClient.finnOppgaver("15", Oppgavetype.Journalføring) }
                .returns(FinnOppgaveResponseDto(1L, listOf(Oppgave(id = 987))))
        every { integrasjonerClient.finnBehandlendeEnhet("123") }
                .returns(listOf(Enhet("654", "Flekkefjord")))
        every { integrasjonerClient.opprettInfotrygdsak(capture(slot)) }
                .returns(OpprettInfotrygdSakResponse())

        sakService.opprettSakOmIngenFinnes("1")

        assertThat(slot.captured).isEqualToComparingFieldByField(opprettInfotrygdSakRequest)
    }

    val opprettInfotrygdSakRequest =
            OpprettInfotrygdSakRequest(fnr = "123",
                                       fagomrade = FAGOMRÅDE_ENSLIG_FORSØRGER,
                                       stonadsklassifisering2 = "OG",
                                       stonadsklassifisering3 = null,
                                       type = SAKSTYPE_SØKNAD,
                                       opprettetAv = "MOTTAK",
                                       opprettetAvOrganisasjonsEnhetsId = null,
                                       mottakerOrganisasjonsEnhetsId = "654",
                                       motattdato = LocalDate.of(2014, 1, 16),
                                       sendBekreftelsesbrev = false,
                                       oppgaveId = "987",
                                       oppgaveOrganisasjonsenhetId = null)


}