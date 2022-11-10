package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.repository.util.findByIdOrThrow
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.familie.prosessering.internal.TaskWorker
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Vi oppdaterer task.metadata i flere tasker, denne sjekker att det er mulig, sånn att ikke blir endringer i prosessering
 * Hvis denne brekker må man vurdere hur de ellers burde oppdateres
 */
internal class TaskTest : IntegrasjonSpringRunnerTest() {

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private lateinit var taskWorker: TaskWorker

    @Autowired
    private lateinit var taskService: TaskService

    @Test
    internal fun `skal oppdatere task med journalpostId etter att doTask er kjørt, i doActualWork, hvis ikke får man optimistic lock failure`() {
        val task = taskService.save(Task(TEST_TASK_TYPE, UUID.randomUUID().toString()))

        taskWorker.markerPlukket(task.id)
        taskWorker.doActualWork(task.id)

        val oppdatertTask = taskService.findById(task.id)
        assertThat(task.metadata["journalpostId"]).isNull()
        assertThat(oppdatertTask.metadata["journalpostId"]).isEqualTo("Nytt verdi")
    }
}

private const val TEST_TASK_TYPE = "TestTask"

@Service
@TaskStepBeskrivelse(taskStepType = TEST_TASK_TYPE, beskrivelse = "")
class TestTask : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        logger.info("Håndterer task id=${task.id}")
        task.metadata.apply {
            this["journalpostId"] = "Nytt verdi"
        }
    }
}
