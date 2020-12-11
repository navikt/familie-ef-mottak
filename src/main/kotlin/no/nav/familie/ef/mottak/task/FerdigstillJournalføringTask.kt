package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = FerdigstillJournalføringTask.TYPE, beskrivelse = "FerdigstillerJournalføring")
class FerdigstillJournalføringTask(private val arkiveringService: ArkiveringService, private val taskRepository: TaskRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        arkiveringService.ferdigstillJournalpost(task.payload)
    }

    override fun onCompletion(task: Task) {
        val nesteTask = Task.nyTask(FerdigstillOppgaveTask.TYPE, task.payload, task.metadata)
        taskRepository.save(nesteTask)
    }

    companion object {
        const val TYPE = "FerdigstillJournalføringTask"
    }
}
