package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.SøknadskvitteringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = LagPdfTask.TYPE, beskrivelse = "Lag PDF kvittering")
class LagPdfKvitteringTask(
    private val søknadskvitteringService: SøknadskvitteringService,
    private val taskService: TaskService
): AsyncTaskStep {
    override fun doTask(task: Task) {
        søknadskvitteringService.hentSøknadOgMapTilGenereltFormat(task.payload)
    }

    override fun onCompletion(task: Task) {
        taskService.save(
            Task(
                TaskType(LagPdfTask.TYPE).nesteHovedflytTask(),
                task.payload,
                task.metadata,
            ),
        )
    }

    companion object {
        const val TYPE = "lagPdfKvittering"
    }
}