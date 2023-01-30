package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Properties
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = ArkiverSøknadTask.TYPE, beskrivelse = "Arkiver søknad")
class ArkiverSøknadTask(
    private val arkiveringService: ArkiveringService,
    private val taskService: TaskService,
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        logger.info("Starter prosessering av task=${task.id}")
        val journalpostId = arkiveringService.journalførSøknad(task.payload)
        logger.info("Oppdaterer metadata til task=${task.id}")
        task.metadata.apply {
            this["journalpostId"] = journalpostId
        }
        logger.info("Journalfør søknad for task=${task.id}")
    }

    override fun onCompletion(task: Task) {
        val nesteTask = Task(TaskType(TYPE).nesteHovedflytTask(), task.payload, task.metadata)
        val sendMeldingTilDittNavTask =
            Task(
                SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE,
                task.payload,
                Properties(task.metadata).apply {
                    this["eventId"] = UUID.randomUUID().toString()
                },
            )

        taskService.saveAll(listOf(nesteTask, sendMeldingTilDittNavTask))
    }

    companion object {

        const val TYPE = "arkiverSøknad"
    }
}
