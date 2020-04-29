package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class SendSøknadTilSakTaskTest {
    val taskRepository: TaskRepository = mockk()
    val sendSøknadTilSakTask: SendSøknadTilSakTask = SendSøknadTilSakTask(mockk(), taskRepository)

    @Test
    fun `Skal gå til SendMeldingTilDittNavTask når SendSøknadTilSakTask er utført`() {
        val slot = slot<Task>()
        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        sendSøknadTilSakTask.onCompletion(Task.nyTask(type = "", payload = "", properties = Properties()))

        assertEquals(SendMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV, slot.captured.taskStepType)
    }
}