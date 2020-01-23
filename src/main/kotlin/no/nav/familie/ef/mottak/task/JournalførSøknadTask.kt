package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.JournalføringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = JournalførSøknadTask.JOURNALFØR_SØKNAD, beskrivelse = "Jornalfør søknad")
class JournalførSøknadTask(private val journalføringService: JournalføringService,
                           private val taskRepository: TaskRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val journalpostId = journalføringService.journalførSøknad(task.payload)
        task.metadata.apply {
            this["journalpostId"] = journalpostId
        }
        taskRepository.saveAndFlush(task)
    }

    override fun onCompletion(task: Task) {
        val nesteTask: Task =
                Task.nyTask(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.payload)
        taskRepository.save(nesteTask)
    }

    companion object {
        const val JOURNALFØR_SØKNAD = "journalførSøknad"
    }

}