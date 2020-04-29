package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.HentJournalpostService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDateTime

@Service
@TaskStepBeskrivelse(taskStepType = HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK,
                     maxAntallFeil = 100,
                     beskrivelse = "Hent saksnummer fra joark")
class LagOppgavbeIGosysTask(private val taskRepository: TaskRepository,
                            private val hentJournalpostService: HentJournalpostService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        try {
            hentJournalpostService.hentSaksnummer(task.payload)
        } catch (notFound: HttpClientErrorException.NotFound) {
            LOG.info("Hent saksnummer returnerte 404 response body={}",
                     notFound.responseBodyAsString)
            val copy = task.copy(triggerTid = LocalDateTime.now().plusMinutes(15))
            taskRepository.save(copy)
            throw notFound
        }
    }

    override fun onCompletion(task: Task) {
        val nesteTask: Task =
                Task.nyTask(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, task.payload, task.metadata)
        taskRepository.save(nesteTask)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(LagOppgavbeIGosysTask::class.java)
        const val HENT_SAKSNUMMER_FRA_JOARK = "hentSaksnummerFraJoark"
    }
}