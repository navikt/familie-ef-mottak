package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.Properties
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = ArkiverSøknadTask.TYPE, beskrivelse = "Arkiver søknad")
class ArkiverSøknadTask(private val arkiveringService: ArkiveringService,
                        private val taskRepository: TaskRepository,
                        private val søknadRepository: SøknadRepository) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        logger.info("Starter prosessering av task=${task.id}")
        val journalpostId = arkiveringService.journalførSøknad(task.payload)
        logger.info("Oppdaterer metadata til task=${task.id}")
        task.metadata.apply {
            this["journalpostId"] = journalpostId
        }
        logger.info("Journalfør søknad for task=${task.id}")
        taskRepository.save(task)
    }

    override fun onCompletion(task: Task) {
        val nesteTask = if (erSøknadOmStønad(task.payload)) {
            Task(TaskType(TYPE).nesteHovedflytTask(), task.payload, task.metadata)
        } else {
            Task(TaskType(TYPE).nesteFallbackTask(), task.payload, task.metadata)
        }
        val sendMeldingTilDittNavTask =
                Task(SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE,
                     task.payload,
                     Properties(task.metadata).apply {
                         this["eventId"] = UUID.randomUUID().toString()
                     })

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