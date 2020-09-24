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

internal class TaskServiceTest {

    private val taskRepository: TaskRepository = mockk(relaxed = true)
    private val soknadRepository: SoknadRepository = mockk(relaxed = true)

    private val scheduledEventService = TaskService(taskRepository, soknadRepository, mockk())

    @Test
    fun `opprettTask oppretter en task for søknad og setter taskOpprettet på søknaden til true`() {
        val soknad = Soknad(dokumenttype = "søknad", søknadJson = "json", fnr = "fnr", taskOpprettet = false)
        val taskSlot = slot<Task>()
        val soknadSlot = slot<Soknad>()
        every { taskRepository.save(capture(taskSlot)) }
                .answers { taskSlot.captured }
        every { soknadRepository.save(capture(soknadSlot)) }
                .answers { soknadSlot.captured }

        scheduledEventService.opprettPdfTaskForSoknad(soknad)

        assertThat(taskSlot.captured.payload).isEqualTo(soknad.id)
        assertThat(soknadSlot.captured.taskOpprettet).isTrue
    }
}