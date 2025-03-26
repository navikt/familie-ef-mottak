package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.PdfKvitteringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = LagPdfKvitteringTask.TYPE, beskrivelse = "Lag pdf-oppsummering av søknad")
class LagPdfKvitteringTask(
    private val pdfKvitteringService: PdfKvitteringService,
    private val taskService: TaskService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        pdfKvitteringService.lagPdf(task.payload)
    }

    override fun onCompletion(task: Task) {
        taskService.save(
            Task(
                TaskType(TYPE).nestePdfKvitteringTask(),
                task.payload,
                task.metadata,
            ),
        )
    }

    companion object {
        const val TYPE = "lagPdfKvittering"
    }
}
