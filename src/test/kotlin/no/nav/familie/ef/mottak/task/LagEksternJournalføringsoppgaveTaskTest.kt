package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.prosessering.domene.Task
import org.junit.jupiter.api.Test
import java.util.Properties

internal class LagEksternJournalføringsoppgaveTaskTest {

    private val ettersendingRepository = mockk<EttersendingRepository>()
    private val søknadRepository = mockk<SøknadRepository>()
    private val oppgaveServie = mockk<OppgaveService>()
    private val lagEksternJournalføringsoppgaveTask = LagEksternJournalføringsoppgaveTask(ettersendingRepository,
                                                                                          oppgaveServie,
                                                                                          søknadRepository)

    @Test
    internal fun `skal opprette ekstern journalføringstask`() {

        every { ettersendingRepository.findByJournalpostId(any()) } returns null
        every { søknadRepository.findByJournalpostId(any()) } returns null
        every { oppgaveServie.lagJournalføringsoppgaveForJournalpostId(any()) } returns 1

        lagEksternJournalføringsoppgaveTask.doTask(Task(type = LagEksternJournalføringsoppgaveTask.TYPE,
                                                        payload = "123",
                                                        properties = Properties())
        )

        verify { oppgaveServie.lagJournalføringsoppgaveForJournalpostId("123") }
    }
}