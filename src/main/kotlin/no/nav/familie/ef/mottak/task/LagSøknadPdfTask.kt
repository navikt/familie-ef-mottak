package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.PdfService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = LagSøknadPdfTask.TYPE, beskrivelse = "Lag pdf-oppsummering av søknad")
class LagSøknadPdfTask(
    private val pdfService: PdfService,
    private val taskService: TaskService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        pdfService.lagPdf(task.payload)
    }

    override fun onCompletion(task: Task) {
        taskService.save(
            Task(
                TaskType(TYPE).nesteHovedflytTask(),
                task.payload,
                task.metadata,
            ),
        )
    }

    companion object {
        const val TYPE = "lagSøknadPdf"
    }
}
