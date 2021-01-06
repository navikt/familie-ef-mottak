package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.PdfService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = LagPdfTask.LAG_PDF, beskrivelse = "Lag pdf")
class LagPdfTask(private val pdfService: PdfService,
                 private val taskRepository: TaskRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        pdfService.lagPdf(task.payload)
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(Task(ArkiverSøknadTask.TYPE,
                                        task.payload,
                                        task.metadata))
    }

    companion object {

        const val LAG_PDF = "lagPdf"
    }

}