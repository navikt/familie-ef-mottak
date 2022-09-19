package no.nav.familie.ef.mottak.task

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.ef.mottak.service.EttersendingService
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException

@Service
@TaskStepBeskrivelse(taskStepType = ArkiverEttersendingTask.TYPE, beskrivelse = "Arkiver ettersending")
class ArkiverEttersendingTask(
    private val arkiveringService: ArkiveringService,
    private val taskRepository: TaskRepository,
    private val ettersendingService: EttersendingService
) : AsyncTaskStep {

    val logger: Logger = LoggerFactory.getLogger(javaClass)
    val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")
    val antallEttersendinger: Counter = Metrics.counter("alene.med.barn.journalposter.ettersending")

    override fun doTask(task: Task) {
        val journalpostId: String = try {
            arkiveringService.journalførEttersending(task.payload)
        } catch (e: HttpClientErrorException.Conflict) {
            logger.error("409 conflict for eksternReferanseId ved journalføring av ettersending. taskId=${task.id}. Se task eller securelog")
            secureLogger.error(
                "409 conflict for eksternReferanseId ved journalføring $task ${e.responseBodyAsString}",
                e
            )
            val ettersending: Ettersending = ettersendingService.hentEttersending(task.payload)
            val callId = task.metadata["callId"].toString()
            arkiveringService.hentJournalpostIdForBrukerOgEksternReferanseId(
                callId,
                Bruker(id = ettersending.fnr, type = BrukerIdType.FNR)
            )?.journalpostId
                ?: error("Fant ikke journalpost for callId (eksternReferanseId) for ettersending $task.payload ")
        } catch (e: Exception) {
            logger.error("Uventet feil ved journalføring av søknad. taskId=${task.id}. Se task eller securelog")
            secureLogger.error("Uventet feil ved journalføring søknad $task", e)
            throw e
        }

        task.metadata.apply {
            this["journalpostId"] = journalpostId
        }
        antallEttersendinger.increment()
    }

    override fun onCompletion(task: Task) {
        val nesteTask = Task(TaskType(TYPE).nesteEttersendingsflytTask(), task.payload, task.metadata)

        taskRepository.save(nesteTask)
    }

    companion object {

        const val TYPE = "arkiverEttersending"
    }
}
