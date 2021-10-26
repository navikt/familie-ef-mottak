package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = LagJournalføringsoppgaveForEttersendingTask.TYPE,
                     beskrivelse = "Lager oppgave i GoSys")
class LagJournalføringsoppgaveForEttersendingTask(private val oppgaveService: OppgaveService,
                                                  private val taskRepository: TaskRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val oppgaveId = oppgaveService.lagJournalføringsoppgaveForEttersendingId(task.payload)
        task.metadata.apply {
            this[LagJournalføringsoppgaveTask.journalføringOppgaveIdKey] = oppgaveId.toString()
        }
        taskRepository.save(task)
    }

    override fun onCompletion(task: Task) {
        taskRepository.save(Task(TaskType(LagPdfTask.TYPE).nesteFallbackTask(),
                                 task.payload,
                                 task.metadata))
    }


    companion object {

        const val TYPE = "lagJournalføringsoppgaveForEttersending"
    }
}
