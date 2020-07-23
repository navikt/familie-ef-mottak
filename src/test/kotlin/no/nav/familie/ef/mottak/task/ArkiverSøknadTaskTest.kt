package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class ArkiverSøknadTaskTest {

    val taskRepository: TaskRepository = mockk()
    val arkiverSøknadTaskTest: ArkiverSøknadTask = ArkiverSøknadTask(mockk(), taskRepository)

    @Test
    fun `Skal gå til LagOppgaveTask når journalføring er utført`() {
        val slot = slot<Task>()
        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        arkiverSøknadTaskTest.onCompletion(Task.nyTask(type = "", payload = "", properties = Properties()))

        assertEquals(LagJournalføringsoppgaveTask.TYPE, slot.captured.taskStepType)
    }
}