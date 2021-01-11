package no.nav.familie.ef.mottak.service

import io.mockk.*
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakRequest
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakResponse
import no.nav.familie.kontrakter.felles.journalpost.*
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
    fun `opprettSakOmIngenFinnes gjør ingenting om sak for overgangsstønad finnes`() {

        every { søknadService.get("1") } returns Soknad(søknadJson = "", dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD, journalpostId = "15", fnr = "123")
        every { integrasjonerClient.finnJournalposter(any()) }
                .returns(listOf(Journalpost(journalpostId = "15",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.FERDIGSTILT,
                                            sak = Sak(fagsakId = "23",
                                                      fagsaksystem = INFOTRYGD),
                                            dokumenter = listOf(DokumentInfo(dokumentInfoId = "123", brevkode="NAV 15-00.02"),
                                                                DokumentInfo(dokumentInfoId = "123", brevkode="NAV 15-00.01"))
                )))

        sakService.opprettSakOmIngenFinnes("1")

        verify { integrasjonerClient.opprettInfotrygdsak(any()) wasNot Called }
    }

    @Test
    fun `opprettSakOmIngenFinnes gjør ingenting om sak for barnetilsyn finnes`() {

        every { søknadService.get("1") } returns Soknad(søknadJson = "", dokumenttype = DOKUMENTTYPE_BARNETILSYN, journalpostId = "15", fnr = "123")
        every { integrasjonerClient.finnJournalposter(any()) }
                .returns(listOf(Journalpost(journalpostId = "15",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.FERDIGSTILT,
                                            sak = Sak(fagsakId = "23",
                                                      fagsaksystem = INFOTRYGD),
                                            dokumenter = listOf(DokumentInfo(dokumentInfoId = "123", brevkode="NAV 15-00.02"))
                )))

        sakService.opprettSakOmIngenFinnes("1")

        verify { integrasjonerClient.opprettInfotrygdsak(any()) wasNot Called }
    }

    @Test
    fun `opprettSakOmIngenFinnes gjør ingenting om sak for skolepenger finnes`() {

        every { søknadService.get("1") } returns Soknad(søknadJson = "", dokumenttype = DOKUMENTTYPE_SKOLEPENGER, journalpostId = "15", fnr = "123")
        every { integrasjonerClient.finnJournalposter(any()) }
                .returns(listOf(Journalpost(journalpostId = "15",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.FERDIGSTILT,
                                            sak = Sak(fagsakId = "23",
                                                      fagsaksystem = INFOTRYGD),
                                            dokumenter = listOf(DokumentInfo(dokumentInfoId = "123", brevkode="NAV 15-00.04"))
                )))

        sakService.opprettSakOmIngenFinnes("1")

        verify { integrasjonerClient.opprettInfotrygdsak(any()) wasNot Called }
    }

    @Test
    fun `opprettSakOmIngenFinnes oppretter sak om ingen finnes for overgangsstønad`() {

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
                                            journalstatus = Journalstatus.FERDIGSTILT,
                                            sak = Sak(fagsakId = "23",
                                                      fagsaksystem = INFOTRYGD),
                                            dokumenter = listOf(DokumentInfo(dokumentInfoId = "123", brevkode="NAV 15-00.02"),
                                                                DokumentInfo(dokumentInfoId = "123", brevkode="NAV 15-00.04")))))
        every { integrasjonerClient.finnOppgaver("15", Oppgavetype.Journalføring) }
                .returns(FinnOppgaveResponseDto(1L, listOf(Oppgave(id = 987))))
        every { integrasjonerClient.finnBehandlendeEnhet("123") }
                .returns(listOf(Enhet("654", "Flekkefjord")))
        every { integrasjonerClient.opprettInfotrygdsak(capture(slot)) }
                .returns(OpprettInfotrygdSakResponse())

        sakService.opprettSakOmIngenFinnes("1")

        assertThat(slot.captured).isEqualToComparingFieldByField(opprettInfotrygdSakRequest("OG"))
    }

    @Test
    fun `opprettSakOmIngenFinnes oppretter sak om ingen finnes for barnetilsyn`() {

        val slot = slot<OpprettInfotrygdSakRequest>()
        every { søknadService.get("1") }
                .returns(Soknad(søknadJson = "",
                                dokumenttype = DOKUMENTTYPE_BARNETILSYN,
                                journalpostId = "15",
                                fnr = "123",
                opprettetTid = LocalDateTime.of(2014, 1, 16, 12, 45)))
        every { integrasjonerClient.finnJournalposter(any()) }
                .returns(listOf(Journalpost(journalpostId = "15",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.FERDIGSTILT,
                                            sak = Sak(fagsakId = "23",
                                                      fagsaksystem = INFOTRYGD),
                                            dokumenter = listOf(DokumentInfo(dokumentInfoId = "123", brevkode="NAV 15-00.01"),
                                                                DokumentInfo(dokumentInfoId = "123", brevkode="NAV 15-00.04")))))
        every { integrasjonerClient.finnOppgaver("15", Oppgavetype.Journalføring) }
                .returns(FinnOppgaveResponseDto(1L, listOf(Oppgave(id = 987))))
        every { integrasjonerClient.finnBehandlendeEnhet("123") }
                .returns(listOf(Enhet("654", "Flekkefjord")))
        every { integrasjonerClient.opprettInfotrygdsak(capture(slot)) }
                .returns(OpprettInfotrygdSakResponse())

        sakService.opprettSakOmIngenFinnes("1")

        assertThat(slot.captured).isEqualToComparingFieldByField(opprettInfotrygdSakRequest("BT"))
    }

    @Test
    fun `opprettSakOmIngenFinnes oppretter sak om ingen finnes for skolepenger`() {

        val slot = slot<OpprettInfotrygdSakRequest>()
        every { søknadService.get("1") }
                .returns(Soknad(søknadJson = "",
                                dokumenttype = DOKUMENTTYPE_SKOLEPENGER,
                                journalpostId = "15",
                                fnr = "123",
                opprettetTid = LocalDateTime.of(2014, 1, 16, 12, 45)))
        every { integrasjonerClient.finnJournalposter(any()) }
                .returns(listOf(Journalpost(journalpostId = "15",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.FERDIGSTILT,
                                            sak = Sak(fagsakId = "23",
                                                      fagsaksystem = INFOTRYGD),
                                            dokumenter = listOf(DokumentInfo(dokumentInfoId = "123", brevkode="NAV 15-00.01"),
                                                                DokumentInfo(dokumentInfoId = "123", brevkode="NAV 15-00.02")))))
        every { integrasjonerClient.finnOppgaver("15", Oppgavetype.Journalføring) }
                .returns(FinnOppgaveResponseDto(1L, listOf(Oppgave(id = 987))))
        every { integrasjonerClient.finnBehandlendeEnhet("123") }
                .returns(listOf(Enhet("654", "Flekkefjord")))
        every { integrasjonerClient.opprettInfotrygdsak(capture(slot)) }
                .returns(OpprettInfotrygdSakResponse())

        sakService.opprettSakOmIngenFinnes("1")

        assertThat(slot.captured).isEqualToComparingFieldByField(opprettInfotrygdSakRequest("UT"))
    }

    private fun opprettInfotrygdSakRequest(stønadsklassifisering: String) =
            OpprettInfotrygdSakRequest(fnr = "123",
                                       fagomrade = FAGOMRÅDE_ENSLIG_FORSØRGER,
                                       stonadsklassifisering2 = stønadsklassifisering,
                                       type = SAKSTYPE_SØKNAD,
                                       opprettetAvOrganisasjonsEnhetsId = "654",
                                       mottakerOrganisasjonsEnhetsId = "654",
                                       mottattdato = LocalDate.of(2014, 1, 16),
                                       sendBekreftelsesbrev = false,
                                       oppgaveId = "987",
                                       oppgaveOrganisasjonsenhetId = null)


}