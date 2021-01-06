package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = ArkiverSøknadTask.TYPE, beskrivelse = "Arkiver søknad")
class ArkiverSøknadTask(private val arkiveringService: ArkiveringService,
                        private val taskRepository: TaskRepository,
                        private val featureToggleService: FeatureToggleService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val journalpostId = arkiveringService.journalførSøknad(task.payload)
        task.metadata.apply {
            this["journalpostId"] = journalpostId
        }
        taskRepository.save(task)
    }

    override fun onCompletion(task: Task) {
        // Når vi begynner å lytte på journalføringshendelser så trenger vi ikke denne tasen.
        if (!featureToggleService.isEnabled("familie-ef-mottak.journalhendelse.behsak")) {
            val nesteTask: Task = Task(LagJournalføringsoppgaveTask.TYPE, task.payload, task.metadata)
            taskRepository.save(nesteTask)
        }
    }

    companion object {

        const val TYPE = "arkiverSøknad"
    }

}