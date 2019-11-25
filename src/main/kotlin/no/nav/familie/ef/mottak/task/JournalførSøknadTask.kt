package no.nav.familie.ef.mottak.task

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
        journalføringService.journalførSøknad(task.payloadId)
    }

    override fun onCompletion(task: Task) {
        val nesteTask: Task =
                Task.nyTask(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.payloadId)
        taskRepository.save(nesteTask)
    }

    companion object {
        const val JOURNALFØR_SØKNAD = "journalførSøknad"
    }

}