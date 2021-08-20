package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.EttersendingVedleggRepository
import no.nav.familie.ef.mottak.service.PdfService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = LagEttersendingPdfTask.TYPE, beskrivelse = "Lag pdf for ettersending")
class LagEttersendingPdfTask(private val pdfService: PdfService,
                             private val taskRepository: TaskRepository,
                             private val ettersendingRepository: EttersendingRepository,
                             private val ettersendingVedleggRepository: EttersendingVedleggRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val ettersending =
                ettersendingRepository.findByIdOrNull(UUID.fromString(task.payload)) ?: error("Kan ikke finne ettersending med id ${task.payload}")
        val vedleggTitler = ettersendingVedleggRepository.findByEttersendingId(ettersending.id.toString()).map { it.tittel }

        pdfService.lagForsideForEttersending(ettersending, vedleggTitler)
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(Task(TaskType(TYPE).nesteEttersendingsflytTask(),
                                        task.payload,
                                        task.metadata))
    }

    companion object {
        const val TYPE = "lagPdfEttersending"
    }

}