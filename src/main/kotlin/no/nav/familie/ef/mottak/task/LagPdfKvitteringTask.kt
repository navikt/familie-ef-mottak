package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.ITextPdfService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = LagPdfKvitteringTask.TYPE, beskrivelse = "Lag PDF kvittering")
class LagPdfKvitteringTask(
    private val iTextPdfService: ITextPdfService,
    private val taskService: TaskService
): AsyncTaskStep {
    override fun doTask(task: Task) {
        iTextPdfService.lagITextPdf(task.payload)
    }

    override fun onCompletion(task: Task) {
        taskService.save(
            Task(
                TaskType(TYPE).nesteNyPdfKvitteringTask(),
                task.payload,
                task.metadata,
            ),
        )
    }

    companion object {
        const val TYPE = "lagPdfKvittering"
    }
}