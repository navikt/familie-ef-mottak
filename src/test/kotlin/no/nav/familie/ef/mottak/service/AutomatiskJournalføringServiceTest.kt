package no.nav.familie.ef.mottak.service

import io.mockk.mockk
import no.nav.familie.kontrakter.felles.ef.StønadType
import org.junit.jupiter.api.Test

internal class AutomatiskJournalføringServiceTest {

    private val automatiskJournalføringService =
        AutomatiskJournalføringService(
            taskRepository = mockk(),
            søknadService = mockk(),
            saksbehandlingClient = mockk()
        )

    @Test
    internal fun `Skal gjøre noe `() {
        // TODO implementer god test
        automatiskJournalføringService.lagFørstegangsbehandlingOgBehandleSakOppgave(
            personIdent = "",
            journalpostId = "",
            stønadstype = StønadType.OVERGANGSSTØNAD
        )
    }
}
