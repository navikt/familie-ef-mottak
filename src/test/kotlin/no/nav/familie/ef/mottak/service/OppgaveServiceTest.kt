package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.OpprettOppgaveMapper
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.Testdata
import no.nav.familie.kontrakter.felles.journalpost.DokumentInfo
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class OppgaveServiceTest {

    private val integrasjonerClient: IntegrasjonerClient = mockk()
    private val søknadService: SøknadService = mockk()
    private val opprettOppgaveMapper = OpprettOppgaveMapper(integrasjonerClient)
    private val oppgaveService: OppgaveService = OppgaveService(integrasjonerClient, søknadService, opprettOppgaveMapper)


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
                                     listOf(DokumentInfo("1", "", "", null, null, null)),
                                     null))
        every { integrasjonerClient.finnOppgaver(any()) } returns FinnOppgaveResponseDto(0L, emptyList())
        every {
            søknadService.get("123")
        } returns Soknad(søknadJson = "{}",
                         dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
                         journalpostId = "999",
                         fnr = Testdata.randomFnr())


        oppgaveService.lagJournalføringsoppgaveForSøknadId("123")

        verify(exactly = 1) {
            integrasjonerClient.lagOppgave(any())
        }
    }


}
