package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.TaskMetricRepository
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.UUID

@ActiveProfiles("local")
internal class TaskMetricServiceTest : IntegrasjonSpringRunnerTest() {

    @Autowired lateinit var taskRepository: TaskRepository
    @Autowired lateinit var taskMetricRepository: TaskMetricRepository
    @Autowired lateinit var søknadRepository: SøknadRepository
    @Autowired lateinit var taskMetricService: TaskMetricService

    @Test
    internal fun `henting av metrics går fint`() {
        taskRepository.deleteAll()
        søknadRepository.save(søknad())
        //oppretter 2 søknader som har opprettet tid nå som ikke skal vises
        søknadRepository.save(søknad(LocalDateTime.now()))
        søknadRepository.save(søknad(LocalDateTime.now()))
        //oppretter 2 søknader som har taskOpprettet = true som ikke skal vises
        søknadRepository.save(søknad(taskOpprettet = true))
        søknadRepository.save(søknad(taskOpprettet = true))

        taskRepository.save(Task("test2", UUID.randomUUID().toString()).copy(status = Status.FEILET))

        assertThat(søknadRepository.countByTaskOpprettetFalseAndOpprettetTidBefore()).isEqualTo(1)
        assertThat(taskMetricRepository.finnFeiledeTasks()).hasSize(1)

        taskMetricService.oppdaterMetrikker()
    }
}
