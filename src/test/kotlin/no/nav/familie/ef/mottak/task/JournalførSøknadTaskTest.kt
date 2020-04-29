package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class JournalførSøknadTaskTest {
    val taskRepository: TaskRepository = mockk()
    val journalførSøknadTaskTest: JournalførSøknadTask = JournalførSøknadTask(mockk(), taskRepository)

    @Test
    fun `Skal gå til LagOppgaveTask når journalføring er utført`() {
        val slot = slot<Task>()
        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        journalførSøknadTaskTest.onCompletion(Task.nyTask(type = "", payload = "", properties = Properties()))

        assertEquals(LagOppgaveTask.LAG_OPPGAVE, slot.captured.taskStepType)
    }
}