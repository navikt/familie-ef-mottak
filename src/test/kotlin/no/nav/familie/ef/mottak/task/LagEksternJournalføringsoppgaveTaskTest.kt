package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.domain.Ettersending
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

    @Test
    internal fun `skal ikke opprette ekstern journalføringstask hvis det finnes en søknad for journalposten`() {

        every { ettersendingRepository.findByJournalpostId(any()) } returns null
        every { søknadRepository.findByJournalpostId(any()) } returns søknad()
        every { oppgaveServie.lagJournalføringsoppgaveForJournalpostId(any()) } returns 1

        lagEksternJournalføringsoppgaveTask.doTask(Task(type = LagEksternJournalføringsoppgaveTask.TYPE,
                                                        payload = "123",
                                                        properties = Properties())
        )

        verify(inverse = true) { oppgaveServie.lagJournalføringsoppgaveForJournalpostId(any()) }
    }

    @Test
    internal fun `skal ikke opprette ekstern journalføringstask hvis det finnes en ettersending for journalposten`() {

        every { ettersendingRepository.findByJournalpostId(any()) } returns Ettersending(ettersendingJson = "{a:1}",
                                                                                         stønadType = "OVERGANGSSTØNAD",
                                                                                         journalpostId = "1",
                                                                                         fnr = "",
                                                                                         taskOpprettet = false)
        every { søknadRepository.findByJournalpostId(any()) } returns null
        every { oppgaveServie.lagJournalføringsoppgaveForJournalpostId(any()) } returns 1

        lagEksternJournalføringsoppgaveTask.doTask(Task(type = LagEksternJournalføringsoppgaveTask.TYPE,
                                                        payload = "123",
                                                        properties = Properties())
        )

        verify(inverse = true) { oppgaveServie.lagJournalføringsoppgaveForJournalpostId(any()) }
    }
}