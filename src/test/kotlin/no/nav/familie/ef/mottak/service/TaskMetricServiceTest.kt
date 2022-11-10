package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.TaskMetricRepository
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

internal class TaskMetricServiceTest : IntegrasjonSpringRunnerTest() {

    @Autowired lateinit var taskService: TaskService

    @Autowired lateinit var taskMetricRepository: TaskMetricRepository

    @Autowired lateinit var søknadRepository: SøknadRepository

    @Autowired lateinit var taskMetricService: TaskMetricService

    @Test
    internal fun `henting av metrics går fint`() {
        taskService.deleteAll(taskService.findAll())
        søknadRepository.insert(søknad())
        // oppretter 2 søknader som har opprettet tid nå som ikke skal vises
        søknadRepository.insert(søknad(LocalDateTime.now()))
        søknadRepository.insert(søknad(LocalDateTime.now()))
        // oppretter 2 søknader som har taskOpprettet = true som ikke skal vises
        søknadRepository.insert(søknad(taskOpprettet = true))
        søknadRepository.insert(søknad(taskOpprettet = true))

        taskService.save(Task("test2", UUID.randomUUID().toString()).copy(status = Status.FEILET))

        assertThat(søknadRepository.countByTaskOpprettetFalseAndOpprettetTidBefore()).isEqualTo(1)
        assertThat(taskMetricRepository.finnFeiledeTasks()).hasSize(1)

        taskMetricService.oppdaterMetrikker()
    }
}
