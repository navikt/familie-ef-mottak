package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = FerdigstillOppgaveTask.TYPE, beskrivelse = "FerdigstillerOppgave")
class FerdigstillOppgaveTask(private val oppgaveService: OppgaveService, private val søknadService: SøknadService, private val taskRepository: TaskRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val soknad: Soknad = søknadService.get(task.payload)
        val journalpostId: String = soknad.journalpostId ?: error("Søknad mangler journalpostId")

        oppgaveService.ferdigstillOppgaveForJournalpost(journalpostId)
    }

    override fun onCompletion(task: Task) {
        val nesteTask = Task.nyTask(LagBehandleSakOppgaveTask.TYPE, task.payload, task.metadata)
        taskRepository.save(nesteTask)
    }


    companion object {
        const val TYPE = "ferdigstillOppgave"
    }
}
