package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("local")
internal class TaskMetricServiceTest : IntegrasjonSpringRunnerTest() {

    @Autowired lateinit var taskRepository: TaskRepository
    @Autowired lateinit var taskMetricService: TaskMetricService

    @Test
    internal fun `henting av metrics går fint`() {
        taskRepository.save(Task.nyTask("test2", UUID.randomUUID().toString()).copy(status = Status.FEILET))
        taskMetricService.oppdaterMetrikker()
    }
}