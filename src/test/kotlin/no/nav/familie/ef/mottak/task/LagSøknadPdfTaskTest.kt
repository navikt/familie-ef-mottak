package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Properties

internal class LagSøknadPdfTaskTest {
    private val taskService: TaskService = mockk()
    private val lagSøknadPdfTask: LagSøknadPdfTask = LagSøknadPdfTask(mockk(), taskService)

    @Test
    fun `Skal gå til JournalførSøknadTask når LagPdfKvitteringTask er utført`() {
        val slot = slot<Task>()
        every {
            taskService.save(capture(slot))
        } answers {
            slot.captured
        }

        lagSøknadPdfTask.onCompletion(Task(type = "", payload = "", properties = Properties()))

        assertEquals(ArkiverSøknadTask.TYPE, slot.captured.type)
    }
}
