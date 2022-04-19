package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TaskProsesseringServiceTest {

    private val taskRepository: TaskRepository = mockk(relaxed = true)
    private val søknadRepository: SøknadRepository = mockk(relaxed = true)
    private val ettersendingRepository: EttersendingRepository = mockk(relaxed = true)

    private val scheduledEventService = TaskProsesseringService(taskRepository, søknadRepository, ettersendingRepository)

    @Test
    fun `startTaskProsessering oppretter en task for søknad og setter taskOpprettet på søknaden til true`() {
        val soknad = søknad()
        val taskSlot = slot<Task>()
        val soknadSlot = slot<Søknad>()
        every { taskRepository.save(capture(taskSlot)) }
                .answers { taskSlot.captured }
        every { søknadRepository.update(capture(soknadSlot)) }
                .answers { soknadSlot.captured }

        scheduledEventService.startTaskProsessering(soknad)

        assertThat(taskSlot.captured.payload).isEqualTo(soknad.id)
        assertThat(soknadSlot.captured.taskOpprettet).isTrue
    }
}