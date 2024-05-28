package no.nav.familie.ef.mottak.task

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = ArkiverEttersendingTask.TYPE, beskrivelse = "Arkiver ettersending")
class ArkiverEttersendingTask(
    private val arkiveringService: ArkiveringService,
    private val taskService: TaskService,
) : AsyncTaskStep {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    val antallEttersendinger: Counter = Metrics.counter("alene.med.barn.journalposter.ettersending")

    override fun doTask(task: Task) {
        val journalpostId: String = arkiveringService.journalførEttersending(task.payload, task.callId)
        task.metadata.apply {
            this["journalpostId"] = journalpostId
        }
        antallEttersendinger.increment()
    }

    override fun onCompletion(task: Task) {
        val nesteTask = Task(TaskType(TYPE).nesteEttersendingsflytTask(), task.payload, task.metadata)

        taskService.save(nesteTask)
    }

    companion object {
        const val TYPE = "arkiverEttersending"
    }
}
