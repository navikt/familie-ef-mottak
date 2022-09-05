package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.kontrakter.ef.journalføring.AutomatiskJournalføringResponse
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AutomatiskJournalføringServiceTest {

    private val saksbehandlingClient = mockk<SaksbehandlingClient>()
    private val automatiskJournalføringService =
        AutomatiskJournalføringService(
            taskRepository = mockk(),
            søknadService = mockk(),
            saksbehandlingClient = saksbehandlingClient
        )

    private val automatiskJournalføringResponse = AutomatiskJournalføringResponse(
        fagsakId = UUID.randomUUID(),
        behandlingId = UUID.randomUUID(),
        behandleSakOppgaveId = 0
    )

    @Test
    internal fun `Skal returnere true når journalføring i ef-sak går bra `() {

        every {
            saksbehandlingClient.lagFørstegangsbehandlingOgBehandleSakOppgave(any())
        } returns automatiskJournalføringResponse

        assertTrue {
            automatiskJournalføringService.lagFørstegangsbehandlingOgBehandleSakOppgave(
                personIdent = "",
                journalpostId = "",
                stønadstype = StønadType.OVERGANGSSTØNAD
            )
        }
    }

    @Test
    internal fun `Skal returnere false når journalføring i ef-sak går bra `() {

        every {
            saksbehandlingClient.lagFørstegangsbehandlingOgBehandleSakOppgave(any())
        } throws RuntimeException("Feil")

        assertFalse {
            automatiskJournalføringService.lagFørstegangsbehandlingOgBehandleSakOppgave(
                personIdent = "",
                journalpostId = "",
                stønadstype = StønadType.OVERGANGSSTØNAD
            )
        }
    }
}
