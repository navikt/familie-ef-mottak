package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_VEDLEGG
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.service.Testdata
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class OppgaveServiceTest {

    val integrasjonerClient: IntegrasjonerClient = mockk()
    val søknadService: SøknadService = mockk()
    val oppgaveService: OppgaveService = OppgaveService(integrasjonerClient, søknadService)

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
        every {
            søknadService.get("123")
        } returns Soknad(søknadJson = "{}",
                         dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
                         journalpostId = "999",
                         fnr = Testdata.randomFnr(),
                         vedlegg = "[]")


        oppgaveService.lagOppgave("123")

        verify(exactly = 1) {
            integrasjonerClient.lagOppgave(any())
        }
    }

    @Test
    fun `Skal ikke opprette oppgave for vedlegg`() {
        every {
            integrasjonerClient.lagOppgave(any())
        } returns OppgaveResponse(oppgaveId = 1)
        every {
            søknadService.get("123")
        } returns Soknad(søknadJson = "{}",
                         dokumenttype = DOKUMENTTYPE_VEDLEGG,
                         journalpostId = "999",
                         fnr = Testdata.randomFnr(),
                         vedlegg = "[]"
        )

        oppgaveService.lagOppgave("123")

        verify(exactly = 0) {
            integrasjonerClient.lagOppgave(any())
        }
    }

    @Test
    fun `Skal ikke opprette oppgave for overgangsstønad før vi støtter dette hos oss`() {
        every {
            integrasjonerClient.lagOppgave(any())
        } returns OppgaveResponse(oppgaveId = 1)
        every {
            søknadService.get("123")
        } returns Soknad(søknadJson = "{}",
                         dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                         journalpostId = "999",
                         fnr = Testdata.randomFnr(),
                         vedlegg = null)

        oppgaveService.lagOppgave("123")

        verify(exactly = 0) {
            integrasjonerClient.lagOppgave(any())
        }
    }

}
