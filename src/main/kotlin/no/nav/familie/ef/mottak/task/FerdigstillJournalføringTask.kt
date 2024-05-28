package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = FerdigstillJournalføringTask.TYPE, beskrivelse = "FerdigstillerJournalføring")
class FerdigstillJournalføringTask(private val arkiveringService: ArkiveringService) : AsyncTaskStep {
    override fun doTask(task: Task) {
        arkiveringService.ferdigstillJournalpost(task.payload)
    }

    companion object {
        const val TYPE = "FerdigstillJournalføringTask"
    }
}
