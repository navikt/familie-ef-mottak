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
class LagJournalføringsoppgaveForEttersendingTask(private val taskRepository: TaskRepository,
                                   private val oppgaveService: OppgaveService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        oppgaveService.lagJournalføringsoppgaveForEttersendingId(task.payload)
    }


    companion object {
        const val TYPE = "lagJournalføringsoppgaveForEttersending"
    }
}
