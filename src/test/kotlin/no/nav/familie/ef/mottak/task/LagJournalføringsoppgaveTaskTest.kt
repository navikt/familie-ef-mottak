package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID

internal class LagJournalføringsoppgaveTaskTest {
    private val taskService: TaskService = mockk()
    private val oppgaveService: OppgaveService = mockk(relaxed = true)
    private val lagJournalføringsoppgaveTask: LagJournalføringsoppgaveTask =
        LagJournalføringsoppgaveTask(taskService, oppgaveService)

    @Test
    fun `skal kalle lagJournalføringsoppgaveForSøknadId hvis task payload er gyldig uuid`() {
        val uuid = UUID.randomUUID().toString()

        every { oppgaveService.lagJournalføringsoppgaveForSøknadId(uuid) } returns 1234L
        every { taskService.save(any()) } answers { firstArg() }
        lagJournalføringsoppgaveTask.doTask(Task(type = "", payload = uuid, properties = Properties()))

        verify { oppgaveService.lagJournalføringsoppgaveForSøknadId(uuid) }
    }
}
