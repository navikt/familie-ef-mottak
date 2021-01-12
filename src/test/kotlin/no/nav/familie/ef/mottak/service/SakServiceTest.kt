package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
    fun `skal ikke kunne opprette infotrygd-sak dersom det allerede eksisterer en sak for stønaden`() {

        val soknad = Soknad(id = "1",søknadJson = "", dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD, journalpostId = "15", fnr = "123")
        every { integrasjonerClient.finnJournalposter(any()) }
                .returns(listOf(Journalpost(journalpostId = "15",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.FERDIGSTILT,
                                            sak = Sak(fagsakId = "23",
                                                      fagsaksystem = INFOTRYGD),
                                            dokumenter = listOf(DokumentInfo(dokumentInfoId = "123", brevkode = "NAV 15-00.02"),
                                                                DokumentInfo(dokumentInfoId = "123", brevkode = "NAV 15-00.01"))
                )))

        assertThat(sakService.kanOppretteInfotrygdSak(soknad)).isFalse()
    }


    @Test
    fun `skal ikke kunne opprette barnetrygd-infotrygd-sak dersom den allerede eksisterer`() {

        val soknad = Soknad(id = "1",søknadJson = "", dokumenttype = DOKUMENTTYPE_BARNETILSYN, journalpostId = "15", fnr = "123")
        every { integrasjonerClient.finnJournalposter(any()) }
                .returns(listOf(Journalpost(journalpostId = "15",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.FERDIGSTILT,
                                            sak = Sak(fagsakId = "23",
                                                      fagsaksystem = INFOTRYGD),
                                            dokumenter = listOf(DokumentInfo(dokumentInfoId = "123", brevkode = "NAV 15-00.02"))
                )))

        assertThat(sakService.kanOppretteInfotrygdSak(soknad)).isFalse()
    }

    @Test
    fun `skal ikke kunne opprette skolepenger-infotrygd-sak dersom den allerede eksisterer`() {

        val soknad = Soknad(id = "1",søknadJson = "", dokumenttype = DOKUMENTTYPE_SKOLEPENGER, journalpostId = "15", fnr = "123")
        every { integrasjonerClient.finnJournalposter(any()) }
                .returns(listOf(Journalpost(journalpostId = "15",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.FERDIGSTILT,
                                            sak = Sak(fagsakId = "23",
                                                      fagsaksystem = INFOTRYGD),
                                            dokumenter = listOf(DokumentInfo(dokumentInfoId = "123", brevkode = "NAV 15-00.04"))
                )))

        assertThat(sakService.kanOppretteInfotrygdSak(soknad)).isFalse()
    }

    @Test
    fun `skal opprette for overgangsstønad`() {

        val slot = slot<OpprettInfotrygdSakRequest>()
        every { søknadService.get("1") }
                .returns(Soknad(id = "1",søknadJson = "",
                                dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                                journalpostId = "15",
                                fnr = "123",
                                opprettetTid = LocalDateTime.of(2014, 1, 16, 12, 45)))
        every { integrasjonerClient.finnBehandlendeEnhet("123") }
                .returns(listOf(Enhet("0315", "Flekkefjord")))
        every { integrasjonerClient.opprettInfotrygdsak(capture(slot)) }
                .returns(OpprettInfotrygdSakResponse())

        sakService.opprettSak("1", "987")

        assertThat(slot.captured).isEqualToComparingFieldByField(opprettInfotrygdSakRequest("OG"))
    }

    @Test
    fun `skal kunne opprette infotrygdsak for overgangsstønad`() {

        val soknad = Soknad(id = "1", søknadJson = "",
                            dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                            journalpostId = "15",
                            fnr = "123",
                            opprettetTid = LocalDateTime.of(2014, 1, 16, 12, 45))
        every { integrasjonerClient.finnJournalposter(any()) }
                .returns(listOf(Journalpost(journalpostId = "15",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.FERDIGSTILT,
                                            sak = Sak(fagsakId = "23",
                                                      fagsaksystem = INFOTRYGD),
                                            dokumenter = listOf(DokumentInfo(dokumentInfoId = "123", brevkode = "NAV 15-00.02"),
                                                                DokumentInfo(dokumentInfoId = "123",
                                                                             brevkode = "NAV 15-00.04")))))

        assertThat(sakService.kanOppretteInfotrygdSak(soknad)).isTrue()
    }

    @Test
    fun `skal kunne opprette infotrygdsak for barnetilsyn`() {

        val soknad = Soknad(id = "1",
                            søknadJson = "",
                            dokumenttype = DOKUMENTTYPE_BARNETILSYN,
                            journalpostId = "15",
                            fnr = "123",
                            opprettetTid = LocalDateTime.of(2014, 1, 16, 12, 45))
        every { søknadService.get("1") }.returns(soknad)
        every { integrasjonerClient.finnJournalposter(any()) }
                .returns(listOf(Journalpost(journalpostId = "15",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.FERDIGSTILT,
                                            sak = Sak(fagsakId = "23",
                                                      fagsaksystem = INFOTRYGD),
                                            dokumenter = listOf(DokumentInfo(dokumentInfoId = "123", brevkode = "NAV 15-00.01"),
                                                                DokumentInfo(dokumentInfoId = "123",
                                                                             brevkode = "NAV 15-00.04")))))

        assertThat(sakService.kanOppretteInfotrygdSak(soknad)).isTrue()

    }

    @Test
    fun `opprettSakOmIngenFinnes oppretter sak om ingen finnes for skolepenger`() {

        val soknad = Soknad(id = "1",
                            søknadJson = "",
                                 dokumenttype = DOKUMENTTYPE_SKOLEPENGER,
                                 journalpostId = "15",
                                 fnr = "123",
                                 opprettetTid = LocalDateTime.of(2014, 1, 16, 12, 45))
        every { søknadService.get("1") }
                .returns(soknad)
        every { integrasjonerClient.finnJournalposter(any()) }
                .returns(listOf(Journalpost(journalpostId = "15",
                                            journalposttype = Journalposttype.I,
                                            journalstatus = Journalstatus.FERDIGSTILT,
                                            sak = Sak(fagsakId = "23",
                                                      fagsaksystem = INFOTRYGD),
                                            dokumenter = listOf(DokumentInfo(dokumentInfoId = "123", brevkode = "NAV 15-00.01"),
                                                                DokumentInfo(dokumentInfoId = "123",
                                                                             brevkode = "NAV 15-00.02")))))
        assertThat(sakService.kanOppretteInfotrygdSak(soknad)).isTrue()
    }

    private fun opprettInfotrygdSakRequest(stønadsklassifisering: String) =
            OpprettInfotrygdSakRequest(fnr = "123",
                                       fagomrade = FAGOMRÅDE_ENSLIG_FORSØRGER,
                                       stonadsklassifisering2 = stønadsklassifisering,
                                       type = SAKSTYPE_SØKNAD,
                                       opprettetAvOrganisasjonsEnhetsId = "0315",
                                       mottakerOrganisasjonsEnhetsId = "0315",
                                       mottattdato = LocalDate.of(2014, 1, 16),
                                       sendBekreftelsesbrev = false,
                                       oppgaveId = "987",
                                       oppgaveOrganisasjonsenhetId = null)


}