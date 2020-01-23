package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.LagPdfService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = LagPdfTask.LAG_PDF, beskrivelse = "Lag pdf")
class LagPdfTask(private val lagPdfService: LagPdfService,
                 private val taskRepository: TaskRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        lagPdfService.lagPdf(task.payload)
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(Task.nyTask(JournalførSøknadTask.JOURNALFØR_SØKNAD,
                                        task.payload,
                                        task.metadata))
    }

    companion object {
        const val LAG_PDF = "lagPdf"
    }

}