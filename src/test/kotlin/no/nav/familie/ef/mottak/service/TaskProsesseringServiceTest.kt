package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TaskProsesseringServiceTest {

    private val taskRepository: TaskRepository = mockk(relaxed = true)
    private val soknadRepository: SoknadRepository = mockk(relaxed = true)

    private val scheduledEventService = TaskProsesseringService(taskRepository, soknadRepository, mockk(relaxed = true))

    @Test
    fun `startTaskProsessering oppretter en task for søknad og setter taskOpprettet på søknaden til true`() {
        val soknad = Soknad(dokumenttype = "søknad", søknadJson = "json", fnr = "fnr", taskOpprettet = false)
        val taskSlot = slot<Task>()
        val soknadSlot = slot<Soknad>()
        every { taskRepository.save(capture(taskSlot)) }
                .answers { taskSlot.captured }
        every { soknadRepository.save(capture(soknadSlot)) }
                .answers { soknadSlot.captured }

        scheduledEventService.startTaskProsessering(soknad)

        assertThat(taskSlot.captured.payload).isEqualTo(soknad.id)
        assertThat(soknadSlot.captured.taskOpprettet).isTrue
    }
}