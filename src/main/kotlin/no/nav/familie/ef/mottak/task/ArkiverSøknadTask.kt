package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = ArkiverSøknadTask.TYPE, beskrivelse = "Arkiver søknad")
class ArkiverSøknadTask(private val arkiveringService: ArkiveringService,
                        private val taskRepository: TaskRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val journalpostId = arkiveringService.journalførSøknad(task.payload)
        task.metadata.apply {
            this["journalpostId"] = journalpostId
        }
        taskRepository.saveAndFlush(task)
    }

    override fun onCompletion(task: Task) {
        val nesteTask: Task = Task.nyTask(LagJournalføringsoppgaveTask.TYPE, task.payload, task.metadata)
        taskRepository.save(nesteTask)
    }

    companion object {
        const val TYPE = "arkiverSøknad"
    }

}