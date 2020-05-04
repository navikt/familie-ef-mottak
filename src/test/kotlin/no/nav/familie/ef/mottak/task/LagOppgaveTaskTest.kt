package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class LagOppgaveTaskTest {

    val taskRepository: TaskRepository = mockk()
    val lagOppgaveTask: LagOppgaveTask = LagOppgaveTask(taskRepository, mockk())

    @Test
    fun `Skal gå til HentSaksnummerFraJoarkTask når LagOppgaveTask er utført`() {
        val slot = slot<Task>()
        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        lagOppgaveTask.onCompletion(Task.nyTask(type = "", payload = "", properties = Properties()))

        assertEquals(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, slot.captured.taskStepType)
    }
}