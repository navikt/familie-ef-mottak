package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.EttersendingService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = EttersendingsslettingTask.TYPE,
    beskrivelse = "Sletter en ettersending fra mottak",
)
class EttersendingsslettingTask(
    private val ettersendingService: EttersendingService,
) : AsyncTaskStep {
    @Transactional
    override fun doTask(task: Task) {
        ettersendingService.slettEttersending(UUID.fromString(task.payload))
    }

    companion object {
        const val TYPE = "ettersendingssletting"
    }
}
