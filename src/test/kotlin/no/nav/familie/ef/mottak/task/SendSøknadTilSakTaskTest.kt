package no.nav.familie.ef.mottak.task

import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Test
import java.util.*

internal class SendSøknadTilSakTaskTest {

    private val taskRepository: TaskRepository = mockk()
    private val sendSøknadTilSakTask: SendSøknadTilSakTask = SendSøknadTilSakTask(mockk(), taskRepository)

    @Test
    fun `Skal ikke opprette noen ny task`() {
        sendSøknadTilSakTask.onCompletion(Task.nyTask(type = "", payload = "", properties = Properties()))
        verify { listOf(taskRepository) wasNot Called }
    }
}
