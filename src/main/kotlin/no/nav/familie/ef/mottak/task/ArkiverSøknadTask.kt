package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
@TaskStepBeskrivelse(taskStepType = ArkiverSøknadTask.TYPE, beskrivelse = "Arkiver søknad")
class ArkiverSøknadTask(private val arkiveringService: ArkiveringService,
                        private val taskRepository: TaskRepository,
                        private val søknadRepository: SøknadRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val journalpostId = arkiveringService.journalførSøknad(task.payload)
        task.metadata.apply {
            this["journalpostId"] = journalpostId
        }
        taskRepository.save(task)
    }

    override fun onCompletion(task: Task) {
        val nesteTask = if (erSøknadOmStønad(task.payload)) {
            Task(TaskType(TYPE).nesteHovedflytTask(), task.payload, task.metadata)
        } else {
            Task(TaskType(TYPE).nesteFallbackTask(), task.payload, task.metadata)
        }
        task.metadata["eventId"] = UUID.randomUUID()
        val sendMeldingTilDittNavTask =
            Task(SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE,
                task.payload,
                task.metadata)

        taskRepository.saveAll(listOf(nesteTask, sendMeldingTilDittNavTask))
    }

    private fun erSøknadOmStønad(søknadId: String): Boolean {
        val soknad = søknadRepository.findByIdOrNull(søknadId) ?: error("Søknad har forsvunnet!")
        return soknad.dokumenttype != DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
    }

    companion object {
        const val TYPE = "arkiverSøknad"
    }

}