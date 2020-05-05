package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class HentSaksnummerFraJoarkTaskTest {

    val taskRepository: TaskRepository = mockk()
    val hentSaksnummerFraJoarkTask: HentSaksnummerFraJoarkTask = HentSaksnummerFraJoarkTask(taskRepository, mockk())

    @Test
    fun `Skal gå til SendSøknadTilSakTask når HentSaksnummerFraJoark er utført`() {
        val slot = slot<Task>()
        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        hentSaksnummerFraJoarkTask.onCompletion(Task.nyTask(type = "", payload = "", properties = Properties()))

        assertEquals(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, slot.captured.taskStepType)
    }
}
