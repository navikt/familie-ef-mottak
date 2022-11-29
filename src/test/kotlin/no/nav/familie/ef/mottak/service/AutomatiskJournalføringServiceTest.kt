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
            taskService = mockk(),
            søknadService = mockk(),
            saksbehandlingClient = saksbehandlingClient
        )

    private val automatiskJournalføringResponse = AutomatiskJournalføringResponse(
        fagsakId = UUID.randomUUID(),
        behandlingId = UUID.randomUUID(),
        behandleSakOppgaveId = 0
    )

    private val mappeId = 1L

    @Test
    internal fun `Skal returnere true når journalføring i ef-sak går bra `() {
        every {
            saksbehandlingClient.journalførAutomatisk(any())
        } returns automatiskJournalføringResponse

        assertTrue {
            automatiskJournalføringService.journalførAutomatisk(
                personIdent = "",
                journalpostId = "",
                stønadstype = StønadType.OVERGANGSSTØNAD,
                mappeId = mappeId
            )
        }
    }

    @Test
    internal fun `Skal returnere false når journalføring i ef-sak feiler `() {
        every {
            saksbehandlingClient.journalførAutomatisk(any())
        } throws RuntimeException("Feil")

        assertFalse {
            automatiskJournalføringService.journalførAutomatisk(
                personIdent = "",
                journalpostId = "",
                stønadstype = StønadType.OVERGANGSSTØNAD,
                mappeId = mappeId
            )
        }
    }
}
