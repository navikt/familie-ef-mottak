package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class HentSaksnummerFraJoarkTaskTest {

    private val taskRepository: TaskRepository = mockk()
    private val featureToggleService: FeatureToggleService = mockk()
    private val hentSaksnummerFraJoarkTask: HentSaksnummerFraJoarkTask = HentSaksnummerFraJoarkTask(taskRepository,
                                                                                                    mockk(),
                                                                                                    featureToggleService)


    @Test
    fun `Skal gå til SendSøknadTilSakTask når HentSaksnummerFraJoark er utført`() {
        val slot = slot<Task>()
        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }
        every {
            featureToggleService.isEnabled(any())
        } returns true

        hentSaksnummerFraJoarkTask.onCompletion(Task.nyTask(type = "", payload = "", properties = Properties()))

        assertEquals(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, slot.captured.taskStepType)
    }
}
