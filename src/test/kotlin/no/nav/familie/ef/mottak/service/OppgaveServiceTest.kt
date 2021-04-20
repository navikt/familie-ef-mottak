package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.OpprettOppgaveMapper
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.IOTestUtil
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.ef.sak.DokumentBrevkode
import no.nav.familie.kontrakter.felles.journalpost.*
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import java.nio.charset.Charset
import kotlin.test.assertEquals

internal class OppgaveServiceTest {

    private val integrasjonerClient: IntegrasjonerClient = mockk()
    private val søknadService: SøknadService = mockk()
    private val sakService: SakService = mockk()
    private val opprettOppgaveMapper = OpprettOppgaveMapper(integrasjonerClient)
    private val oppgaveService: OppgaveService =
            OppgaveService(integrasjonerClient, søknadService, opprettOppgaveMapper, sakService)


    @BeforeEach
    private fun init() {
        every {
            integrasjonerClient.hentAktørId(any())
        } returns Testdata.randomAktørId()
    }


    @Test
    fun `Skal kalle integrasjonsklient ved opprettelse av oppgave`() {
        every {
            integrasjonerClient.lagOppgave(any())
        } returns OppgaveResponse(oppgaveId = 1)
        every { integrasjonerClient.hentJournalpost("999") }
                .returns(Journalpost("999",
                                     Journalposttype.I,
                                     Journalstatus.MOTTATT,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     listOf(DokumentInfo("1", "", "", null, null, null)),
                                     null))
        every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0L, emptyList())
        every {
            søknadService.get("123")
        } returns Søknad(søknadJson = "{}",
                         dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
                         journalpostId = "999",
                         fnr = Testdata.randomFnr())


        oppgaveService.lagJournalføringsoppgaveForSøknadId("123")

        verify(exactly = 1) {
            integrasjonerClient.lagOppgave(any())
        }
    }

    @Test
    fun `Opprett oppgave med enhet NAY hvis opprettOppgave-kall får feil som følge av at enhet ikke blir funnet for bruker`() {

        val opprettOppgaveRequest = opprettOppgaveMapper.toJournalføringsoppgave(journalpost)
        every {
            integrasjonerClient.lagOppgave(opprettOppgaveRequest)
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          "Server error",
                                          IOTestUtil.readFile("opprett_oppgave_feilet.json").toByteArray(),
                                          Charset.defaultCharset())

        val forventetOpprettOppgaveRequestMedNayEnhet = opprettOppgaveRequest.copy(enhetsnummer = "4489")
        every {
            integrasjonerClient.lagOppgave(forventetOpprettOppgaveRequestMedNayEnhet)
        } answers {
            OppgaveResponse(1)
        }

        every {
            integrasjonerClient.finnOppgaver(any(), any())
        } returns FinnOppgaveResponseDto(0, listOf())

        val oppgaveResponse = oppgaveService.lagJournalføringsoppgave(journalpost)


        assertEquals(1, oppgaveResponse)
    }

    @Test
    fun `Opprett oppgave med enhet NAY hvis opprettBahandleSak-kall får feil som følge av at enhet ikke blir funnet for bruker`() {

        val behandleSakOppgaveRequest = opprettOppgaveMapper.toBehandleSakOppgave(journalpost, "")


        every {
            integrasjonerClient.lagOppgave(behandleSakOppgaveRequest)
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                          "Server error",
                                          IOTestUtil.readFile("opprett_oppgave_feilet.json").toByteArray(),
                                          Charset.defaultCharset())

        val forventetOpprettOppgaveRequestMedNayEnhet = behandleSakOppgaveRequest.copy(enhetsnummer = "4489")
        every {
            integrasjonerClient.lagOppgave(forventetOpprettOppgaveRequestMedNayEnhet)
        } answers {
            OppgaveResponse(1)
        }

        every {
            integrasjonerClient.finnOppgaver(any(), any())
        } returns FinnOppgaveResponseDto(0, listOf())

        val oppgaveResponse = oppgaveService.lagBehandleSakOppgave(journalpost, "")


        assertEquals(1, oppgaveResponse)
    }

    private val journalpost =
            Journalpost(
                    journalpostId = "111111111",
                    journalposttype = Journalposttype.I,
                    journalstatus = Journalstatus.MOTTATT,
                    tema = "ENF",
                    behandlingstema = "ab0071",
                    tittel = "abrakadabra",
                    bruker = Bruker(type = BrukerIdType.AKTOERID, id = "3333333333333"),
                    journalforendeEnhet = "4817",
                    kanal = "SKAN_IM",
                    sak = Sak(null, null, null),
                    dokumenter =
                    listOf(
                            DokumentInfo(
                                    dokumentInfoId = "12345",
                                    tittel = "Tittel",
                                    brevkode = DokumentBrevkode.OVERGANGSSTØNAD.verdi,
                                    dokumentvarianter = listOf(Dokumentvariant(variantformat = "ARKIV"))
                            )
                    )
            )


}