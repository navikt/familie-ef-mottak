package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.HentJournalpostService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = HentJournalpostIdFraJoarkTask.HENT_JOURNALPOSTID_FRA_JOARK,
                     beskrivelse = "Hent journapostId fra joark basert på kanalreferanseId")
class HentJournalpostIdFraJoarkTask @Autowired constructor(private val taskRepository: TaskRepository,
                                                           hentJournalpostService: HentJournalpostService) : AsyncTaskStep {

    private val hentJournalpostService: HentJournalpostService

    override fun doTask(task: Task) {
        hentJournalpostService.oppdaterSøknadMedJournalpostId(task.payload, task.callId)
    }

    override fun onCompletion(task: Task) {
        val nesteTask: Task =
                Task.nyTask(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.payload)

        taskRepository.save(nesteTask)
    }

    companion object {
        const val HENT_JOURNALPOSTID_FRA_JOARK = "hentJournalpostIdFraJoarkTask"
    }

    init {
        this.hentJournalpostService = hentJournalpostService
    }
}