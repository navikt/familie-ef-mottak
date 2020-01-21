package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.HentJournalpostService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK,
                     maxAntallFeil = 100,
                     beskrivelse = "Hent saksnummer fra joark")
class HentSaksnummerFraJoarkTask(private val taskRepository: TaskRepository,
                                 private val hentJournalpostService: HentJournalpostService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        hentJournalpostService.hentSaksnummer(task.payload)
    }

    override fun onCompletion(task: Task) {
        val nesteTask: Task =
                Task.nyTask(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, task.payload)
        taskRepository.save(nesteTask)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(HentSaksnummerFraJoarkTask::class.java)
        const val HENT_SAKSNUMMER_FRA_JOARK = "hentSaksnummerFraJoark"
    }
}