package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Properties

internal class LagPdfTaskTest {

    private val taskRepository: TaskRepository = mockk()
    private val lagPdfTask: LagPdfTask = LagPdfTask(mockk(), taskRepository)

    @Test
    fun `Skal gå til JournalførSøknadTask når LagPdfTask er utført`() {
        val slot = slot<Task>()
        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        lagPdfTask.onCompletion(Task(type = "", payload = "", properties = Properties()))

        assertEquals(ArkiverSøknadTask.TYPE, slot.captured.type)
    }
}
