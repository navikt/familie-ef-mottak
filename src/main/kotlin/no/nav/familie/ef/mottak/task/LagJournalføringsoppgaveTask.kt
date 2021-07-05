package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = LagJournalføringsoppgaveTask.TYPE,
                     beskrivelse = "Lager oppgave i GoSys")
class LagJournalføringsoppgaveTask(private val taskRepository: TaskRepository,
                                   private val oppgaveService: OppgaveService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        oppgaveService.lagJournalføringsoppgaveForSøknadId(task.payload)
    }

    override fun onCompletion(task: Task) {
        val hentSaksnummerFraJoarkTask = Task(HentSaksnummerFraJoarkTask.TYPE, task.payload, task.metadata)
        taskRepository.save(hentSaksnummerFraJoarkTask)
    }

    companion object {
        const val TYPE = "lagJournalføringsoppgave"
    }
}
