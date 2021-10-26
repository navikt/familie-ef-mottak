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
        val oppgaveId = oppgaveService.lagJournalføringsoppgaveForSøknadId(task.payload)
        task.metadata.apply {
            this[journalføringOppgaveIdKey] = oppgaveId.toString()
        }
        taskRepository.save(task)
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(Task(TaskType(LagPdfTask.TYPE).nesteFallbackTask(),
                                 task.payload,
                                 task.metadata))
    }


    companion object {
        const val journalføringOppgaveIdKey = "journalføringOppgaveId"
        const val TYPE = "lagJournalføringsoppgave"
    }
}
