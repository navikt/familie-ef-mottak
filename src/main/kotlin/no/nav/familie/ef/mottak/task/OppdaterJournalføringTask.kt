package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = OppdaterJournalføringTask.TYPE,
                     beskrivelse = "Oppdaterer journalføring med saksinfo")
class OppdaterJournalføringTask(private val taskRepository: TaskRepository,
                                private val arkiveringService: ArkiveringService) : AsyncTaskStep {


    override fun doTask(task: Task) {
        arkiveringService.oppdaterJournalpost(task.payload)
    }

    override fun onCompletion(task: Task) {
        val nesteTask = Task.nyTask(FerdigstillJournalføringTask.TYPE, task.payload, task.metadata)
        taskRepository.save(nesteTask)
    }

    companion object {
        const val TYPE = "oppdaterJournalføring"
    }
}
